package com.smarttodo.clickbook.service;

import com.smarttodo.clickbook.entity.User;
import com.smarttodo.clickbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    // Инициализация при старте приложения
    @PostConstruct
    public void init() {
        createDefaultUsers(); // Создает пользователей только если их нет
        verifyExistingUsers(); // Проверяет существующих пользователей
    }

    // Создание тестовых пользователей только если таблица пустая
    private void createDefaultUsers() {
        long userCount = userRepository.count();

        if (userCount == 0) {

            // Администратор
            User admin = new User();
            admin.setUsername("admin");
            admin.setFullName("Администратор КликБук");
            admin.setEmail("admin@clickbook.ru");
            admin.setRole("ROLE_ADMIN");
            admin.setPassword(passwordService.hashPassword("Admin123!"));
            userRepository.save(admin);

            // Тестовый пользователь
            User user = new User();
            user.setUsername("user");
            user.setFullName("Лебединская Надежда");
            user.setEmail("user@clickbook.ru");
            user.setRole("ROLE_USER");
            user.setPassword(passwordService.hashPassword("User123!"));
            userRepository.save(user);
        } else {
            System.out.println("Пользователи существуют");
        }
    }

    // Проверка существующих пользователей
    private void verifyExistingUsers() {

        // Проверка администратора
        Optional<User> adminOpt = userRepository.findByUsername("admin");
        if (adminOpt.isPresent()) {
            User admin = adminOpt.get();
            boolean adminPasswordValid = passwordService.verifyPassword("Admin123!", admin.getPassword());
        } else {
            System.out.println("⚠️ Администратор не найден");
        }

        // Проверка пользователя
        Optional<User> userOpt = userRepository.findByUsername("user");
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean userPasswordValid = passwordService.verifyPassword("User123!", user.getPassword());
        } else {
            System.out.println("⚠️ Пользователь не найден");
        }
    }

    // Основные методы

    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User register(User user) {
        // Проверка существования пользователя
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Пользователь с логином '" + user.getUsername() + "' уже существует");
        }

        if (user.getEmail() != null && !user.getEmail().isEmpty() &&
                userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Пользователь с email '" + user.getEmail() + "' уже существует");
        }

        // Валидация пароля
        if (!isPasswordStrong(user.getPassword())) {
            throw new RuntimeException("Пароль должен содержать минимум 8 символов, " +
                    "включая цифры, буквы в верхнем и нижнем регистре, и специальные символы");
        }

        // Хешируем пароль
        String hashedPassword = passwordService.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        // Всегда устанавливаем роль USER для новых регистраций
        user.setRole("ROLE_USER");

        // Сохраняем в БД
        User savedUser = userRepository.save(user);

        return savedUser;
    }

    public Optional<User> login(String username, String password) {
        System.out.println("🔑 Попытка входа");
        System.out.println("Логин: " + username);

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()) {
            User foundUser = user.get();

            // Проверка пароля
            if (passwordService.verifyPassword(password, foundUser.getPassword())) {
                // Обновляем lastLogin
                userRepository.save(foundUser);

                return Optional.of(foundUser);
            } else {
                System.out.println("❌ Неверный пароль для пользователя: " + username);
            }
        } else {
            System.out.println("❌ Пользователь не найден: " + username);
        }

        return Optional.empty();
    }

    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Проверяем старый пароль
            if (passwordService.verifyPassword(oldPassword, user.getPassword())) {
                if (!isPasswordStrong(newPassword)) {
                    throw new RuntimeException("Новый пароль не соответствует требованиям безопасности");
                }
                // Хешируем новый пароль
                user.setPassword(passwordService.hashPassword(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    public boolean isPasswordStrong(String password) {
        return passwordService.isPasswordStrong(password);
    }
}