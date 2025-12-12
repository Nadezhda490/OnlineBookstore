document.addEventListener('DOMContentLoaded', function() {
    console.log('📚 Страница управления книгами загружена');

    // Функция показа уведомлений
    function showNotification(message, type = 'success') {
        // Создаем элемент уведомления
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} notification-alert`;
        notification.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="fas fa-${type === 'success' ? 'check' : 'exclamation'}-circle me-2 fs-5"></i>
                <span>${message}</span>
                <button type="button" class="btn-close ms-auto" onclick="this.parentElement.parentElement.remove()"></button>
            </div>
        `;

        document.body.appendChild(notification);

        setTimeout(() => notification.classList.add('show'), 10);
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }

    // Автоматическое скрытие алертов через 5 секунд
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        if (!alert.classList.contains('notification-alert')) {
            setTimeout(() => {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }, 5000);
        }
    });

    // Подсветка строк при наведении
    const rows = document.querySelectorAll('.books-table tbody tr');
    rows.forEach(row => {
        if (!row.querySelector('.empty-table')) {
            row.addEventListener('mouseenter', function() {
                this.style.backgroundColor = '#fce4ec';
            });
            row.addEventListener('mouseleave', function() {
                this.style.backgroundColor = '';
            });
        }
    });
});

// Экспорт функций для использования в других скриптах
window.confirmDelete = confirmDelete;