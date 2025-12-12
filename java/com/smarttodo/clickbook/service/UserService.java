package com.smarttodo.clickbook.service;

import com.smarttodo.clickbook.entity.User;
import com.smarttodo.clickbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    // ================== АУТЕНТИФИКАЦИЯ ==================

    public User authenticate(String username, String password) {
        System.out.println("=== 🔐 ПОПЫТКА ВХОДА: " + username + " ===");

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()) {
            User foundUser = user.get();
            String dbHash = foundUser.getPassword();

            // ВАЖНО: используем verifyPassword из PasswordService
            boolean isValid = passwordService.verifyPassword(password, dbHash);

            if (isValid) {
                System.out.println("✅ АУТЕНТИФИКАЦИЯ УСПЕШНА: " + username);
                return foundUser;
            } else {
                System.out.println("❌ НЕВЕРНЫЙ ПАРОЛЬ для: " + username);
            }
        } else {
            System.out.println("❌ ПОЛЬЗОВАТЕЛЬ НЕ НАЙДЕН: " + username);
        }

        return null;
    }

    // ================== ОСТАЛЬНЫЕ МЕТОДЫ ==================

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));
        userRepository.delete(user);
    }

    public List<User> getRegularUsers() {
        return userRepository.findAll().stream()
                .filter(user -> "ROLE_USER".equals(user.getRole()))
                .collect(Collectors.toList());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
    }

    public User saveUser(User user) {
        // Если это обновление существующего пользователя
        if (user.getId() != null) {
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Обновляем только разрешенные поля
            if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                existingUser.setFullName(user.getFullName());
            }

            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                // Проверяем уникальность email
                Optional<User> userWithSameEmail = userRepository.findByEmail(user.getEmail());
                if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(user.getId())) {
                    throw new RuntimeException("Email уже используется другим пользователем");
                }
                existingUser.setEmail(user.getEmail());
            }

            // Пароль НЕ трогаем, если он не был явно указан
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                // Если пароль был передан - шифруем его
                String hashedPassword = passwordService.hashPassword(user.getPassword());
                existingUser.setPassword(hashedPassword);
            }

            // Сохраняем обновленного пользователя
            return userRepository.save(existingUser);

        } else {
            // Это новый пользователь
            if (user.getPassword() != null) {
                String hashedPassword = passwordService.hashPassword(user.getPassword());
                user.setPassword(hashedPassword);
            }
            return userRepository.save(user);
        }
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));
    }

    public User updateUserByAdmin(Long userId, User updatedUser) {
        User user = getUserById(userId);

        if (updatedUser.getFullName() != null) {
            user.setFullName(updatedUser.getFullName());
        }

        if (updatedUser.getEmail() != null) {
            // Проверяем, не занят ли email другим пользователем
            Optional<User> existingUser = userRepository.findByEmail(updatedUser.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new RuntimeException("Email уже используется другим пользователем");
            }
            user.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getRole() != null) {
            user.setRole(updatedUser.getRole());
        }

        return userRepository.save(user);
    }

    public User updateProfile(Long userId, String fullName, String email) {
        User user = getUserById(userId);

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }

        if (email != null && !email.trim().isEmpty()) {
            // Проверяем, не занят ли email другим пользователем
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new RuntimeException("Email уже используется другим пользователем");
            }
            user.setEmail(email.trim());
        }

        return userRepository.save(user);
    }
}