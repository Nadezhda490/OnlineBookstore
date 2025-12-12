// Утилитарные функции
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} notification-alert`;
    notification.innerHTML = `
        <div class="d-flex justify-content-between align-items-center">
            <span>${message}</span>
            <button type="button" class="btn-close" onclick="this.parentElement.parentElement.remove()"></button>
        </div>
    `;

    document.body.appendChild(notification);

    // Анимация появления
    setTimeout(() => notification.classList.add('show'), 100);

    // Автоудаление
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Форматирование цены
function formatPrice(price) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB'
    }).format(price);
}

// Загрузка рекомендаций
function loadRecommendations(bookId, type = 'similar') {
    fetch('/books/' + bookId + '/recommendations?type=' + type)
        .then(response => response.json())
        .then(books => {
            const container = document.getElementById('recommendationsContainer');
            if (container && books.length > 0) {
                container.innerHTML = books.map(book => `
                <div class="recommendation-card">
                    <img src="${book.imageUrl}" alt="${book.title}">
                    <h6>${book.title}</h6>
                    <small class="text-muted">${book.author}</small>
                    <div class="mt-2">
                        <span class="price">${formatPrice(book.priceDigital)}</span>
                    </div>
                    <a href="/book/${book.id}" class="btn btn-sm btn-outline-primary mt-2">Подробнее</a>
                </div>
            `).join('');
            }
        });
}

// Функция выхода из системы
function logout() {
    if (confirm('Вы уверены, что хотите выйти?')) {
        fetch('/api/auth/logout', {
            method: 'POST'
        })
            .then(response => response.json())
            .then(data => {
                if (data.message) {
                    // Перенаправляем на главную страницу
                    window.location.href = '/';
                }
            })
            .catch(error => {
                console.error('Ошибка выхода:', error);
                window.location.href = '/';
            });
    }
}

window.logout = logout;
document.head.appendChild(style);