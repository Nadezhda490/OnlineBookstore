package com.smarttodo.clickbook.service;

import com.smarttodo.clickbook.entity.*;
import com.smarttodo.clickbook.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private BookService bookService;

    // Создание заказа из корзины
    public Order createOrder(String username, String deliveryCity, String deliveryService) {
        User user = userService.findByUsername(username);
        List<CartItem> cartItems = cartService.getCartItems(username);

        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Корзина пуста");
        }

        // Создаем новый заказ
        Order order = new Order();
        order.setUser(user);
        order.setDeliveryCity(deliveryCity);
        order.setDeliveryService(deliveryService);
        order.setStatus("ОПЛАЧЕН");
        order.setPaymentStatus("ОПЛАЧЕН");

        // Рассчитываем общую сумму
        double totalAmount = 0.0;

        for (CartItem cartItem : cartItems) {
            if (cartItem.getBook() != null) {
                // Создаем элемент заказа
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setBook(cartItem.getBook());
                orderItem.setBookType(cartItem.getBookType());
                orderItem.setQuantity(cartItem.getQuantity());

                // Устанавливаем цену
                double price = "DIGITAL".equals(cartItem.getBookType()) ?
                        cartItem.getBook().getPriceDigital() :
                        cartItem.getBook().getPricePrinted();
                orderItem.setPrice(price);

                // Добавляем в список элементов заказа
                order.getItems().add(orderItem);

                // Добавляем к общей сумме
                totalAmount += price * cartItem.getQuantity();
            }
        }

        order.setTotalAmount(totalAmount);

        // Сохраняем заказ (с каскадным сохранением элементов)
        Order savedOrder = orderRepository.save(order);

        // Очищаем корзину после успешного сохранения заказа
        cartService.clearCart(username);

        return savedOrder;
    }

    // Получение заказов пользователя
    public List<Order> getUserOrders(String username) {
        return orderRepository.findByUserUsernameOrderByCreatedAtDesc(username);
    }

    // Получение всех заказов (для администратора)
    public List<Order> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();

            // Отладка
            if (orders == null) {
                System.out.println("❌ Заказы не найдены (null)");
                return new ArrayList<>();
            }

            for (int i = 0; i < Math.min(3, orders.size()); i++) {
                Order order = orders.get(i);
            }

            return orders;
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения заказов: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Order> getOrdersByUsername(String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return new ArrayList<>();
            }

            List<Order> orders = orderRepository.findByUserUsernameOrderByCreatedAtDesc(username);

            return orders != null ? orders : new ArrayList<>();

        } catch (Exception e) {
            System.err.println("❌ Ошибка получения заказов пользователя " + username + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Получение заказа по ID
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }

    // Получение заказа по ID для пользователя
    public Order getOrderById(Long orderId, String username) {
        User user = userService.findByUsername(username);
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            Order foundOrder = order.get();
            if (!foundOrder.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Нет доступа к этому заказу");
            }
            return foundOrder;
        } else {
            throw new RuntimeException("Заказ не найден");
        }
    }

    // Получение заказов по ID пользователя
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Обновление статуса заказа
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    // Удаление заказа (только для администратора)
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Заказ не найден");
        }
        orderRepository.deleteById(orderId);
    }

    // Подсчет количества заказов пользователя
    public long countUserOrders(String username) {
        User user = userService.findByUsername(username);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).size();
    }

    // Подсчет общей суммы покупок пользователя
    public double getUserTotalSpent(String username) {
        User user = userService.findByUsername(username);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }
}