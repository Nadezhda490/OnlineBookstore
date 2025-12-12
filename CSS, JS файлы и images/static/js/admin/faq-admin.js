// Автоматическое скрытие уведомлений через 5 секунд
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(() => {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(alert => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);
});

// Инициализация счетчиков символов
document.addEventListener('DOMContentLoaded', function() {
    const questionInput = document.querySelector('input[th\\:field="*{question}"]');
    const answerInput = document.querySelector('textarea[th\\:field="*{answer}"]');

    if (questionInput) {
        updateCharacterCount('question', questionInput.value, 500);
    }
    if (answerInput) {
        updateCharacterCount('answer', answerInput.value, 2000);
    }

    // Автоматическое обновление предпросмотра при вводе
    questionInput?.addEventListener('input', updatePreview);
    answerInput?.addEventListener('input', updatePreview);
});

// Обновление счетчика символов
function updateCharacterCount(fieldId, text, maxLength) {
    const length = text.length;
    const countElement = document.getElementById(fieldId + '-chars');
    const counterElement = document.getElementById(fieldId + '-count');

    if (countElement) {
        countElement.textContent = length;
    }

    if (counterElement) {
        counterElement.classList.remove('warning', 'danger');

        if (length > maxLength * 0.9) {
            counterElement.classList.add('danger');
        } else if (length > maxLength * 0.7) {
            counterElement.classList.add('warning');
        }
    }
}

// Обновление предпросмотра
function updatePreview() {
    const questionInput = document.querySelector('input[th\\:field="*{question}"]');
    const answerInput = document.querySelector('textarea[th\\:field="*{answer}"]');

    const questionPreview = document.getElementById('question-preview');
    const answerPreview = document.getElementById('answer-preview');

    if (questionInput && questionPreview) {
        questionPreview.textContent = questionInput.value || 'Вопрос появится здесь';
    }

    if (answerInput && answerPreview) {
        // Простая обработка HTML тегов для предпросмотра
        let answerText = answerInput.value || 'Ответ появится здесь';
        // Заменяем переносы строк на <br>
        answerText = answerText.replace(/\n/g, '<br>');
        answerPreview.innerHTML = answerText;
    }
}

// Валидация формы перед отправкой
document.querySelector('form').addEventListener('submit', function(e) {
    const questionInput = document.querySelector('input[th\\:field="*{question}"]');
    const answerInput = document.querySelector('textarea[th\\:field="*{answer}"]');
    const categorySelect = document.querySelector('select[th\\:field="*{category}"]');

    let isValid = true;
    let errorMessage = '';

    // Проверка вопроса
    if (!questionInput.value.trim()) {
        isValid = false;
        errorMessage += '• Введите вопрос\n';
        questionInput.classList.add('is-invalid');
    } else {
        questionInput.classList.remove('is-invalid');
    }

    // Проверка ответа
    if (!answerInput.value.trim()) {
        isValid = false;
        errorMessage += '• Введите ответ\n';
        answerInput.classList.add('is-invalid');
    } else {
        answerInput.classList.remove('is-invalid');
    }

    // Проверка категории
    if (!categorySelect.value) {
        isValid = false;
        errorMessage += '• Выберите категорию\n';
        categorySelect.classList.add('is-invalid');
    } else {
        categorySelect.classList.remove('is-invalid');
    }

    if (!isValid) {
        e.preventDefault();
        alert('Пожалуйста, исправьте ошибки:\n\n' + errorMessage);
    }
});