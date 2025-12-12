package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.entity.User;
import com.smarttodo.clickbook.service.UserService;
import com.smarttodo.clickbook.service.UserSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionService userSessionService;

    @GetMapping("/login")
    public String loginForm(Model model,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout) {

        if (error != null) {
            model.addAttribute("error", "Неверный логин или пароль");
        }

        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        System.out.println("=== 🚀 LOGIN ПОПЫТКА ===");
        System.out.println("   Логин: '" + username + "'");
        System.out.println("   Пароль: '" + password + "'");

        User user = userService.authenticate(username, password);

        if (user != null) {

            // Сохраняем пользователя в сессию
            userSessionService.setCurrentUser(user, session);
            session.setAttribute("CURRENT_USER", user);
            session.setAttribute("username", user.getUsername());

            // Устанавливаем время жизни сессии (30 минут)
            session.setMaxInactiveInterval(30 * 60);

            System.out.println("✅ Сессия создана. ID: " + session.getId());
            System.out.println("✅ Пользователь в сессии: " + session.getAttribute("username"));

            return "redirect:/";
        } else {
            System.out.println("❌ ЛОГИН НЕУДАЧЕН");
            model.addAttribute("error", "Неверный логин или пароль");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        try {
            // Логируем для отладки
            System.out.println("🚪 Выход из системы. Сессия: " + (session != null ? session.getId() : "null"));

            // Просто инвалидируем сессию
            if (session != null) {
                session.invalidate();
                System.out.println("✅ Сессия инвалидирована");
            }

            // Делаем редирект на страницу входа с параметром
            return "redirect:/login?logout=true";

        } catch (Exception e) {
            System.err.println("⚠️ Ошибка при выходе: " + e.getMessage());
            return "redirect:/";
        }
    }
}