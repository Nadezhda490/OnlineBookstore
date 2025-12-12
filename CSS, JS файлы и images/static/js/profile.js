// Функции для работы с профилем

// Валидация формы профиля
function validateProfileForm() {
    const fullName = document.getElementById('fullName').value;
    const email = document.getElementById('email').value;
    const birthDate = document.getElementById('birthDate').value;

    let isValid = true;
    let errorMessage = '';

    // Проверка ФИО
    if (!fullName || fullName.trim().length < 2) {
        isValid = false;
        errorMessage += 'ФИО должно содержать минимум 2 символа\n';
    }

    // Проверка email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        isValid = false;
        errorMessage += 'Введите корректный email\n';
    }

    if (!isValid) {
        alert('Ошибка валидации:\n' + errorMessage);
        return false;
    }

    return true;
}

// Изменение пароля
function changePassword() {
    const oldPassword = document.getElementById('oldPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // Валидация
    if (!oldPassword || !newPassword || !confirmPassword) {
        showNotification('Заполните все поля', 'warning');
        return;
    }

    if (newPassword !== confirmPassword) {
        showNotification('Новые пароли не совпадают', 'error');
        return;
    }

    if (newPassword.length < 8) {
        showNotification('Новый пароль должен содержать минимум 8 символов', 'error');
        return;
    }

    // Отправка на сервер
    fetch('/profile/change-password', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            oldPassword: oldPassword,
            newPassword: newPassword
        })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showNotification('Пароль успешно изменен', 'success');
                // Очищаем поля
                document.getElementById('oldPassword').value = '';
                document.getElementById('newPassword').value = '';
                document.getElementById('confirmPassword').value = '';
                // Закрываем модальное окно
                const modal = bootstrap.Modal.getInstance(document.getElementById('changePasswordModal'));
                if (modal) {
                    modal.hide();
                }
            } else {
                showNotification('Ошибка: ' + data.error, 'error');
            }
        })
        .catch(error => {
            console.error('Ошибка смены пароля:', error);
            showNotification('Ошибка смены пароля', 'error');
        });
}

// Проверка сложности пароля
function checkPasswordStrength(password) {
    const strength = {
        length: password.length >= 8,
        lowercase: /[a-z]/.test(password),
        uppercase: /[A-Z]/.test(password),
        numbers: /\d/.test(password),
        special: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)
    };

    let score = 0;
    for (const key in strength) {
        if (strength[key]) score++;
    }

    return {
        score: score,
        strength: strength
    };
}

// Отображение силы пароля
function displayPasswordStrength(password) {
    const strengthBar = document.getElementById('passwordStrength');
    const strengthText = document.getElementById('passwordStrengthText');

    if (!password) {
        strengthBar.style.width = '0%';
        strengthBar.className = 'progress-bar';
        strengthText.textContent = '';
        return;
    }

    const result = checkPasswordStrength(password);
    const percentage = (result.score / 5) * 100;

    strengthBar.style.width = percentage + '%';

    let color = 'bg-danger';
    let text = 'Слабый';

    if (percentage >= 60) {
        color = 'bg-warning';
        text = 'Средний';
    }
    if (percentage >= 80) {
        color = 'bg-success';
        text = 'Сильный';
    }

    strengthBar.className = `progress-bar ${color}`;
    strengthText.textContent = text;
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    // Обработчик переключателя темы
    const themeSwitch = document.getElementById('themeSwitch');
    if (themeSwitch) {
        themeSwitch.addEventListener('change', function() {
            const theme = this.checked ? 'dark' : 'light';
            updateTheme(theme);
        });
    }

    // Проверка силы пароля в реальном времени
    const newPasswordInput = document.getElementById('newPassword');
    if (newPasswordInput) {
        newPasswordInput.addEventListener('input', function() {
            displayPasswordStrength(this.value);
        });
    }

    // Валидация даты рождения
    const birthDateInput = document.getElementById('birthDate');
    if (birthDateInput) {
        // Устанавливаем максимальную дату (сегодня)
        const today = new Date().toISOString().split('T')[0];
        birthDateInput.max = today;
    }

    // Инициализация модальных окон
    const changePasswordModal = document.getElementById('changePasswordModal');
    if (changePasswordModal) {
        changePasswordModal.addEventListener('hidden.bs.modal', function() {
            // Очищаем поля при закрытии
            document.getElementById('oldPassword').value = '';
            document.getElementById('newPassword').value = '';
            document.getElementById('confirmPassword').value = '';
            displayPasswordStrength('');
        });
    }
});

// Экспорт функций для использования в HTML
window.toggleTheme = toggleTheme;
window.validateProfileForm = validateProfileForm;
window.previewAvatar = previewAvatar;
window.uploadAvatar = uploadAvatar;
window.changePassword = changePassword;
window.displayPasswordStrength = displayPasswordStrength;