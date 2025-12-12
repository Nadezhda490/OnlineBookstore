// Валидация формы регистрации
function validateUsername() {
    const username = document.getElementById('username');
    const helpText = document.getElementById('usernameHelp');

    if (username.value.length >= 5) {
        username.classList.remove('is-invalid');
        username.classList.add('is-valid');
        helpText.classList.remove('text-danger');
        helpText.classList.add('text-success');
        helpText.textContent = 'Логин подходит';
        return true;
    } else {
        username.classList.remove('is-valid');
        username.classList.add('is-invalid');
        helpText.classList.remove('text-success');
        helpText.classList.add('text-danger');
        helpText.textContent = 'Логин должен содержать минимум 5 символов';
        return false;
    }
}

function validatePassword() {
    const password = document.getElementById('password').value;

    // Проверка длины
    const lengthReq = document.getElementById('reqLength');
    if (password.length >= 8) {
        lengthReq.classList.remove('requirement-not-met');
        lengthReq.classList.add('requirement-met');
    } else {
        lengthReq.classList.remove('requirement-met');
        lengthReq.classList.add('requirement-not-met');
    }

    // Проверка специальных символов
    const specialReq = document.getElementById('reqSpecial');
    if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
        specialReq.classList.remove('requirement-not-met');
        specialReq.classList.add('requirement-met');
    } else {
        specialReq.classList.remove('requirement-met');
        specialReq.classList.add('requirement-not-met');
    }

    // Проверка цифр
    const numberReq = document.getElementById('reqNumber');
    if (/\d/.test(password)) {
        numberReq.classList.remove('requirement-not-met');
        numberReq.classList.add('requirement-met');
    } else {
        numberReq.classList.remove('requirement-met');
        numberReq.classList.add('requirement-not-met');
    }

    // Проверка заглавных букв
    const upperReq = document.getElementById('reqUpper');
    if (/[A-ZА-Я]/.test(password)) {
        upperReq.classList.remove('requirement-not-met');
        upperReq.classList.add('requirement-met');
    } else {
        upperReq.classList.remove('requirement-met');
        upperReq.classList.add('requirement-not-met');
    }

    validatePasswordMatch();

    return password.length >= 8 &&
        /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password) &&
        /\d/.test(password) &&
        /[A-ZА-Я]/.test(password);
}

function validatePasswordMatch() {
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const helpText = document.getElementById('passwordMatchHelp');

    if (confirmPassword === '' || password === '') {
        helpText.classList.remove('text-success', 'text-danger');
        helpText.classList.add('text-muted');
        helpText.textContent = 'Пароли должны совпадать';
        return false;
    }

    if (password === confirmPassword) {
        document.getElementById('confirmPassword').classList.remove('is-invalid');
        document.getElementById('confirmPassword').classList.add('is-valid');
        helpText.classList.remove('text-danger', 'text-muted');
        helpText.classList.add('text-success');
        helpText.textContent = 'Пароли совпадают';
        return true;
    } else {
        document.getElementById('confirmPassword').classList.remove('is-valid');
        document.getElementById('confirmPassword').classList.add('is-invalid');
        helpText.classList.remove('text-success', 'text-muted');
        helpText.classList.add('text-danger');
        helpText.textContent = 'Пароли не совпадают';
        return false;
    }
}

function validateForm() {
    const isUsernameValid = validateUsername();
    const isPasswordValid = validatePassword();
    const isPasswordMatch = validatePasswordMatch();

    return isUsernameValid && isPasswordValid && isPasswordMatch;
}

// Инициализация при загрузке
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('username').addEventListener('input', validateUsername);
    document.getElementById('password').addEventListener('input', validatePassword);
    document.getElementById('confirmPassword').addEventListener('input', validatePasswordMatch);
});