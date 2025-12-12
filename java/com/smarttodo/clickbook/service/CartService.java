package com.smarttodo.clickbook.service;

import com.smarttodo.clickbook.entity.CartItem;
import com.smarttodo.clickbook.entity.User;
import com.smarttodo.clickbook.entity.Book;
import com.smarttodo.clickbook.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    public CartItem addToCart(Long bookId, String username, String bookType) {
        User user = userService.findByUsername(username);
        Book book = bookService.getBookById(bookId);

        // Ищем все записи с такой же книгой и типом
        List<CartItem> existingItems = cartRepository.findByUserIdAndBookIdAndBookType(
                user.getId(), bookId, bookType);

        if (!existingItems.isEmpty()) {
            // Если нашли существующие записи
            if (existingItems.size() == 1) {
                // Если только одна запись - увеличиваем количество
                CartItem item = existingItems.get(0);
                item.setQuantity(item.getQuantity() + 1);
                return cartRepository.save(item);
            } else {
                // Если несколько записей (дубликаты) - объединяем их

                // Выбираем первую запись для сохранения
                CartItem mainItem = existingItems.get(0);

                // Суммируем количество всех записей + 1 (новая книга)
                int totalQuantity = existingItems.stream()
                        .mapToInt(CartItem::getQuantity)
                        .sum() + 1;

                mainItem.setQuantity(totalQuantity);

                // Удаляем остальные записи (дубликаты)
                for (int i = 1; i < existingItems.size(); i++) {
                    cartRepository.delete(existingItems.get(i));
                }
                return cartRepository.save(mainItem);
            }
        } else {
            // Если записей нет - создаем новую запись в корзину
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setBook(book);
            newItem.setBookType(bookType);
            newItem.setQuantity(1);
            return cartRepository.save(newItem);
        }
    }

    public List<CartItem> getCartItems(String username) {
        User user = userService.findByUsername(username);
        return cartRepository.findByUserIdWithBooks(user.getId());
    }

    // Дополнительный метод для получения элементов корзины
    public List<CartItem> getCartItemsByUserId(Long userId) {
        return cartRepository.findByUserIdWithBooks(userId);
    }

    public void updateQuantity(Long itemId, Integer quantity, String username) {
        User user = userService.findByUsername(username);
        Optional<CartItem> item = cartRepository.findById(itemId);
        if (item.isPresent()) {
            CartItem cartItem = item.get();
            if (!cartItem.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Доступ запрещен");
            }

            if (quantity <= 0) {
                cartRepository.delete(cartItem);
            } else {
                cartItem.setQuantity(quantity);
                cartRepository.save(cartItem);
            }
        } else {
            throw new RuntimeException("Товар в корзине не найден");
        }
    }

    public void removeFromCart(Long itemId, String username) {
        User user = userService.findByUsername(username);
        Optional<CartItem> item = cartRepository.findById(itemId);
        if (item.isPresent()) {
            CartItem cartItem = item.get();
            if (!cartItem.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Доступ запрещен");
            }
            cartRepository.delete(cartItem);
        } else {
            throw new RuntimeException("Товар в корзине не найден");
        }
    }

    public void clearCart(String username) {
        User user = userService.findByUsername(username);
        cartRepository.deleteByUserId(user.getId());
    }

    public Integer getCartItemCount(String username) {
        User user = userService.findByUsername(username);
        Integer count = cartRepository.countByUserId(user.getId());
        return count != null ? count : 0;
    }
}