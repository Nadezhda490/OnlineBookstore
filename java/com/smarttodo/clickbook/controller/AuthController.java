package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.entity.User;
import com.smarttodo.clickbook.service.AuthService;
import com.smarttodo.clickbook.service.UserSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserSessionService sessionService;

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                           @RequestParam String confirmPassword,
                           Model model,
                           HttpSession session) {
        try {
            // Проверка паролей
            if (!user.getPassword().equals(confirmPassword)) {
                model.addAttribute("error", "Пароли не совпадают");
                return "register";
            }

            // Регистрация
            User registered = authService.register(user);

            // Автоматический вход после регистрации
            sessionService.setCurrentUser(registered, session);

            return "redirect:/";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }

    // Выход
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        sessionService.logout(session);
        return "redirect:/login?logout=true";
    }

    @GetMapping("/check-username/{username}")
    @ResponseBody  // Оставляем для AJAX проверок
    public Map<String, Boolean> checkUsername(@PathVariable String username) {
        boolean exists = authService.checkUsernameExists(username);
        return Map.of("exists", exists);
    }

    @GetMapping("/check-email/{email}")
    @ResponseBody
    public Map<String, Boolean> checkEmail(@PathVariable String email) {
        boolean exists = authService.checkEmailExists(email);
        return Map.of("exists", exists);
    }

    // Текущий пользователь
    @GetMapping("/current-user")
    @ResponseBody
    public User getCurrentUser(HttpSession session) {
        return sessionService.getCurrentUser(session);
    }
}