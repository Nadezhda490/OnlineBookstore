// Скрипты для главной страницы

// Функция обновления ВСЕХ счетчиков корзины
function updateCartCounter(count) {
    console.log('🛒 Обновление счетчика на:', count);

    // 1. Обновляем значок в навигации
    const cartBadges = document.querySelectorAll('.cart-badge');
    cartBadges.forEach(badge => {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'flex' : 'none';
    });

    // 2. Обновляем текст в карточке "Корзина" на главной странице
    const cartCountText = document.getElementById('cart-count-text');
    if (cartCountText) {
        cartCountText.textContent = count > 0 ?
            `Товаров: ${count} в корзине` :
            'Товаров: 0';

        // Добавляем анимацию
        cartCountText.classList.add('text-updated');
        setTimeout(() => {
            cartCountText.classList.remove('text-updated');
        }, 300);
    }

    // 3. Также обновляем другие элементы с классом cart-counter (если есть)
    document.querySelectorAll('.cart-counter').forEach(element => {
        element.textContent = count;
    });
}

// Функция добавления в корзину (с обновлением карточки)
function addToCart(bookId, bookType, buttonElement) {
    console.log('📚 Добавление в корзину:', bookId, bookType);

    const button = buttonElement || event.target.closest('button');
    const originalText = button.innerHTML;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
    button.disabled = true;

    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'bookId=' + bookId + '&bookType=' + (bookType || 'DIGITAL')
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.error || 'Ошибка сервера');
                });
            }
            return response.json();
        })
        .then(data => {
            button.innerHTML = originalText;
            button.disabled = false;

            if (data.success) {
                showNotification('Книга добавлена в корзину!', 'success');

                // ✅ ВАЖНО: Обновляем счетчик СРАЗУ
                updateCartCounter(data.count || 0);

                // Для надежности - перепроверяем через секунду
                setTimeout(() => {
                    loadCartCount();
                }, 1000);
            } else {
                showNotification(data.error || 'Ошибка добавления', 'error');
            }
        })
        .catch(error => {
            console.error('❌ Ошибка сети:', error);
            button.innerHTML = originalText;
            button.disabled = false;
            showNotification('Ошибка добавления в корзину: ' + error.message, 'error');
        });
}

// Функция показа уведомления
function showNotification(message, type = 'success') {
    const oldNotifications = document.querySelectorAll('.notification-alert');
    oldNotifications.forEach(n => n.remove());

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

// Загрузка счетчика при старте
function loadCartCount() {
    fetch('/cart/count')
        .then(response => response.json())
        .then(data => {
            console.log('📊 Начальное количество товаров:', data);
            updateCartCounter(data.count || 0);
        })
        .catch(error => {
            console.error('❌ Ошибка загрузки счетчика:', error);
        });
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    console.log('🏠 Главная страница загружена');

    // Загружаем счетчик корзины сразу
    loadCartCount();

    // Обработчик для кнопок "В корзину" на главной странице
    document.addEventListener('click', function(e) {
        const addToCartBtn = e.target.closest('.add-to-cart-btn');
        if (addToCartBtn) {
            e.preventDefault();

            // Ищем ID книги разными способами
            let bookId = addToCartBtn.getAttribute('data-book-id') ||
                addToCartBtn.closest('[data-book-id]')?.getAttribute('data-book-id') ||
                addToCartBtn.closest('.book-card-uniform')?.querySelector('[data-book-id]')?.getAttribute('data-book-id');

            if (bookId) {
                const bookType = addToCartBtn.getAttribute('data-book-type') || 'DIGITAL';
                addToCart(bookId, bookType, addToCartBtn);
            } else {
                console.warn('⚠️ Кнопка не имеет data-book-id');
            }
        }
    });
});

// Периодическая проверка корзины (каждые 5 секунд)
function startCartPolling() {
    setInterval(() => {
        fetch('/cart/count')
            .then(response => response.json())
            .then(data => {
                updateCartCounter(data.count || 0);
            });
    }, 5000); // 5 секунд
}

// Запускаем при загрузке
document.addEventListener('DOMContentLoaded', function() {
    startCartPolling();
});

// Экспорт функций для использования в HTML
window.addToCart = addToCart;
window.showNotification = showNotification;
window.updateCartCounter = updateCartCounter;