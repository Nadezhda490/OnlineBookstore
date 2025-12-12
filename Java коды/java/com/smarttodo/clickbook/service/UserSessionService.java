package com.smarttodo.clickbook.service;

import com.smarttodo.clickbook.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class UserSessionService {

    private static final String USER_SESSION_KEY = "CURRENT_USER";

    // ДЛЯ MVC Controller (с HttpSession)
    public User getCurrentUser(HttpSession session) {
        if (session == null) {
            System.out.println("⚠️ Сессия null в getCurrentUser");
            return null;
        }

        User user = (User) session.getAttribute(USER_SESSION_KEY);

        return user;
    }

    // ДЛЯ RestController (работает без HttpSession)
    public User getCurrentUser() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(false);

            if (session != null) {
                User user = (User) session.getAttribute(USER_SESSION_KEY);
                if (user != null) {
                    return user;
                }
            }
            return null;
        } catch (Exception e) {
            System.out.println("⚠️ Нет сессии в API: " + e.getMessage());
            return null;
        }
    }

    public void setCurrentUser(User user, HttpSession session) {
        if (session != null && user != null) {
            session.setAttribute(USER_SESSION_KEY, user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userId", user.getId());

            // Проверяем сохранение
            User savedUser = (User) session.getAttribute(USER_SESSION_KEY);
        }
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.removeAttribute(USER_SESSION_KEY);
            session.removeAttribute("username");
            session.removeAttribute("userId");
            session.invalidate();
        }
    }

    public boolean isLoggedIn(HttpSession session) {
        User user = getCurrentUser(session);
        boolean loggedIn = user != null;
        return loggedIn;
    }

    // Проверка является ли пользователь администратором
    public boolean isAdmin(HttpSession session) {
        User user = getCurrentUser(session);
        boolean isAdmin = user != null && "ROLE_ADMIN".equals(user.getRole());
        return isAdmin;
    }

    // Получение ID текущего пользователя
    public Long getCurrentUserId(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null ? user.getId() : null;
    }

    // Получение имени текущего пользователя
    public String getCurrentUsername(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null ? user.getUsername() : null;
    }
}