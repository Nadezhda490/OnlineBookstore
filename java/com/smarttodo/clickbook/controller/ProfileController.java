package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.entity.User;
import com.smarttodo.clickbook.service.UserService;
import com.smarttodo.clickbook.service.UserSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionService userSessionService;

    @GetMapping
    public String profilePage(Model model, HttpSession session) {
        User currentUser = userSessionService.getCurrentUser(session);

        if (currentUser == null) {
            System.out.println("❌ Пользователь не авторизован, редирект на /login");
            return "redirect:/login";
        }

        // Для админов - редирект на панель управления
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            System.out.println("👑 Администратор, редирект на /admin/dashboard");
            return "redirect:/admin/dashboard";
        }

        // Получаем актуальные данные пользователя из БД
        User user = userService.findByUsername(currentUser.getUsername());

        model.addAttribute("user", user);

        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String email,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        User currentUser = userSessionService.getCurrentUser(session);
        if (currentUser == null) {
            System.out.println("❌ Пользователь не в сессии!");
            return "redirect:/login";
        }

        try {
            // Создаем объект для обновления
            User userToUpdate = new User();
            userToUpdate.setId(currentUser.getId()); // ВАЖНО!
            userToUpdate.setFullName(fullName);
            userToUpdate.setEmail(email);

            // Сохраняем
            User updatedUser = userService.saveUser(userToUpdate);

            // Обновляем в сессии
            userSessionService.setCurrentUser(updatedUser, session);

            redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлен");

        } catch (Exception e) {
            System.err.println("❌ Ошибка при обновлении: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Ошибка обновления профиля: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // В ProfileController добавим проверочный метод
    @GetMapping("/test")
    @ResponseBody
    public String testSession(HttpSession session) {
        User currentUser = userSessionService.getCurrentUser(session);
        if (currentUser != null) {
            return "✅ Пользователь в сессии: " + currentUser.getUsername() +
                    ", ФИО: " + currentUser.getFullName() +
                    ", Email: " + currentUser.getEmail();
        } else {
            return "❌ Пользователь не найден в сессии";
        }
    }
}