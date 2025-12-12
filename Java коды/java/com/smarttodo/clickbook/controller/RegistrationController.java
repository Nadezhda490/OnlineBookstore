package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.entity.User;
import com.smarttodo.clickbook.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    @Autowired
    private AuthService authService;

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping
    public String registerUser(@ModelAttribute User user,
                               @RequestParam String confirmPassword,
                               Model model) {
        try {
            System.out.println("=== 📝 РЕГИСТРАЦИЯ НОВОГО ПОЛЬЗОВАТЕЛЯ ===");
            System.out.println("ФИО: " + user.getFullName());
            System.out.println("Логин: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Пароль: " + user.getPassword());
            System.out.println("Подтверждение: " + confirmPassword);

            // Проверка обязательных полей
            if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
                model.addAttribute("error", "ФИО обязательно для заполнения");
                model.addAttribute("user", user);
                return "register";
            }

            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                model.addAttribute("error", "Логин обязателен для заполнения");
                model.addAttribute("user", user);
                return "register";
            }

            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                model.addAttribute("error", "Email обязателен для заполнения");
                model.addAttribute("user", user);
                return "register";
            }

            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                model.addAttribute("error", "Пароль обязателен для заполнения");
                model.addAttribute("user", user);
                return "register";
            }

            // Проверка подтверждения пароля
            if (!user.getPassword().equals(confirmPassword)) {
                model.addAttribute("error", "Пароли не совпадают");
                model.addAttribute("user", user);
                return "register";
            }

            // Проверка сложности пароля
            if (!authService.isPasswordStrong(user.getPassword())) {
                model.addAttribute("error",
                        "Пароль должен содержать минимум 8 символов, включая цифры, " +
                                "буквы в верхнем и нижнем регистре, и специальные символы (!@#$%^&*)");
                model.addAttribute("user", user);
                return "register";
            }

            // Проверка длины логина
            if (user.getUsername().length() < 5) {
                model.addAttribute("error", "Логин должен содержать минимум 5 символов");
                model.addAttribute("user", user);
                return "register";
            }

            // ✅ ВСЕГДА устанавливаем роль USER для новых регистраций
            user.setRole("ROLE_USER");
            System.out.println("Установлена роль: " + user.getRole());

            // Регистрация пользователя
            User registeredUser = authService.register(user);
            System.out.println("✅ Регистрация успешна! ID: " + registeredUser.getId());

            model.addAttribute("success",
                    "Регистрация успешна! Теперь вы можете войти в систему.");
            return "login";

        } catch (RuntimeException e) {
            System.err.println("❌ Ошибка регистрации: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "register";
        } catch (Exception e) {
            System.err.println("❌ Неизвестная ошибка: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Внутренняя ошибка сервера");
            model.addAttribute("user", user);
            return "register";
        }
    }
}