package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.service.*;
import com.smarttodo.clickbook.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private CartService cartService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private FAQService faqService;

    @GetMapping("/")
    public String homePage(Model model, HttpSession session) {
        if (userSessionService.isLoggedIn(session)) {
            User user = userSessionService.getCurrentUser(session);
            model.addAttribute("user", user);

            if (user != null && !"ROLE_ADMIN".equals(user.getRole())) {
                // Для обычных пользователей
                String username = user.getUsername();
                model.addAttribute("cartItemCount", cartService.getCartItemCount(username));

                // Получаем книги для рекомендаций
                try {
                    var recommendedBooks = bookService.getRandomRecommendedBooks(6);
                    model.addAttribute("recommendedBooks", recommendedBooks);
                } catch (Exception e) {
                    System.err.println("Ошибка получения книг: " + e.getMessage());
                    model.addAttribute("recommendedBooks", new java.util.ArrayList<>());
                }

            } else {
                // Для администраторов
                model.addAttribute("cartItemCount", 0);
                model.addAttribute("recommendedBooks", new java.util.ArrayList<>());

                // Статистика для админов (добавляем в модель)
                if (user != null && "ROLE_ADMIN".equals(user.getRole())) {
                    model.addAttribute("admin", user);
                    model.addAttribute("totalBooks", bookService.getAllBooks().size());
                    model.addAttribute("totalUsers", userService.getRegularUsers().size());
                    model.addAttribute("totalOrders", orderService.getAllOrders().size());
                    model.addAttribute("totalFAQs", faqService.getAllFAQs().size()); // Используем getAllFAQs()
                }
            }

            return "dashboard";
        } else {
            return "index";
        }
    }

    @GetMapping("/dashboard")
    public String dashboardPage(Model model, HttpSession session) {
        return homePage(model, session);
    }
}