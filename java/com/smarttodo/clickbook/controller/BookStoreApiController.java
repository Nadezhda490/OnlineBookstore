package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.entity.*;
import com.smarttodo.clickbook.repository.*;
import com.smarttodo.clickbook.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BookStoreApiController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private HttpServletRequest request;

    // Получение текущего пользователя
    private User getCurrentUser(Principal principal) {
        try {
            if (principal != null) {
                String username = principal.getName();
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    return user;
                }
            }

            HttpSession session = request.getSession(false);
            if (session != null) {
                User sessionUser = (User) session.getAttribute("CURRENT_USER");
                if (sessionUser != null) {
                    return sessionUser;
                }
            }

            User sessionServiceUser = userSessionService.getCurrentUser();
            if (sessionServiceUser != null) {
                return sessionServiceUser;
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    // Книги
    @GetMapping("/books")
    public ResponseEntity<?> getBooks(Principal principal) {
        try {
            List<Book> books = bookService.getAllBooks();

            List<Map<String, Object>> bookDtos = books.stream().map(book -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", book.getId());
                dto.put("title", book.getTitle());
                dto.put("author", book.getAuthor());
                dto.put("description", book.getDescription());
                dto.put("priceDigital", book.getPriceDigital());
                dto.put("pricePrinted", book.getPricePrinted());
                dto.put("imageUrl", book.getImageUrl());

                if (book.getCategory() != null) {
                    dto.put("category", book.getCategory().getName());
                }

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(bookDtos);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка загрузки книг"));
        }
    }

    // Корзина
    @GetMapping("/cart")
    public ResponseEntity<?> getCart(Principal principal) {
        try {
            User currentUser = getCurrentUser(principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Требуется авторизация"));
            }

            List<CartItem> cartItems = cartService.getCartItems(currentUser.getUsername());

            List<Map<String, Object>> cartDtos = cartItems.stream().map(item -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", item.getId());
                dto.put("quantity", item.getQuantity());
                dto.put("bookType", item.getBookType());

                if (item.getBook() != null) {
                    Map<String, Object> bookInfo = new HashMap<>();
                    bookInfo.put("id", item.getBook().getId());
                    bookInfo.put("title", item.getBook().getTitle());
                    bookInfo.put("author", item.getBook().getAuthor());
                    bookInfo.put("priceDigital", item.getBook().getPriceDigital());
                    bookInfo.put("pricePrinted", item.getBook().getPricePrinted());
                    bookInfo.put("imageUrl", item.getBook().getImageUrl());
                    dto.put("book", bookInfo);
                }

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(cartDtos);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка загрузки корзины"));
        }
    }

    // Добавление в корзину
    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> cartData, Principal principal) {
        try {
            User currentUser = getCurrentUser(principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Требуется авторизация"));
            }

            Long bookId = Long.valueOf(cartData.get("bookId").toString());
            String bookType = (String) cartData.get("bookType");

            System.out.println("📦 Добавление в корзину: " + bookId + ", тип: " + bookType + ", пользователь: " + currentUser.getUsername());

            cartService.addToCart(bookId, currentUser.getUsername(), bookType);

            Integer count = cartService.getCartItemCount(currentUser.getUsername());

            return ResponseEntity.ok(Map.of(
                    "message", "Книга добавлена в корзину",
                    "count", count
            ));

        } catch (Exception e) {
            System.err.println("❌ Ошибка добавления в корзину: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/cart/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId, Principal principal) {
        try {
            User currentUser = getCurrentUser(principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Требуется авторизация"));
            }

            cartService.removeFromCart(itemId, currentUser.getUsername());
            return ResponseEntity.ok(Map.of("message", "Товар удален из корзины"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/cart/count")
    public ResponseEntity<?> getCartCount(Principal principal) {
        try {
            User currentUser = getCurrentUser(principal);
            if (currentUser == null) {
                return ResponseEntity.ok(Map.of("count", 0));
            }

            Integer count = cartService.getCartItemCount(currentUser.getUsername());
            return ResponseEntity.ok(Map.of("count", count != null ? count : 0));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("count", 0));
        }
    }

    // Заказы
    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(Principal principal) {
        try {
            User currentUser = getCurrentUser(principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Требуется авторизация"));
            }

            List<Order> orders = orderService.getUserOrders(currentUser.getUsername());

            List<Map<String, Object>> orderDtos = orders.stream().map(order -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", order.getId());
                dto.put("totalAmount", order.getTotalAmount());
                dto.put("deliveryCity", order.getDeliveryCity());
                dto.put("deliveryService", order.getDeliveryService());
                dto.put("createdAt", order.getCreatedAt());

                List<Map<String, Object>> itemDtos = order.getItems().stream().map(item -> {
                    Map<String, Object> itemDto = new HashMap<>();
                    itemDto.put("bookTitle", item.getBook().getTitle());
                    itemDto.put("bookAuthor", item.getBook().getAuthor());
                    itemDto.put("quantity", item.getQuantity());
                    itemDto.put("bookType", item.getBookType());
                    return itemDto;
                }).collect(Collectors.toList());
                dto.put("items", itemDtos);

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(orderDtos);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка загрузки заказов"));
        }
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderData, Principal principal) {
        try {
            User currentUser = getCurrentUser(principal);
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Требуется авторизация"));
            }

            List<CartItem> cartItems = cartService.getCartItems(currentUser.getUsername());

            String deliveryCity = (String) orderData.get("deliveryCity");
            String deliveryService = (String) orderData.get("deliveryService");

            Order order = orderService.createOrder(
                    currentUser.getUsername(),
                    deliveryCity,
                    deliveryService
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Заказ создан",
                    "orderId", order.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}