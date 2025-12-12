// Функция добавления в корзину
function addToCart(bookId, bookType, buttonElement) {
    console.log('📚 Добавление в корзину:', { bookId, bookType });

    // Находим кнопку
    const button = buttonElement;
    const originalText = button.innerHTML;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
    button.disabled = true;

    // Отправляем запрос
    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'bookId=' + bookId + '&bookType=' + bookType
    })
        .then(response => {
            console.log('📡 Статус ответа:', response.status);
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.error || 'Ошибка сервера');
                });
            }
            return response.json();
        })
        .then(data => {
            console.log('✅ Ответ сервера:', data);

            // Восстанавливаем кнопку
            button.innerHTML = originalText;
            button.disabled = false;

            if (data.success) {
                const typeName = bookType === 'DIGITAL' ? 'Электронная' : 'Печатная';
                showNotification(`${typeName} книга добавлена в корзину!`, 'success');
                updateCartCounter(data.count || 0);
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
    // Удаляем старые уведомления
    const oldNotifications = document.querySelectorAll('.notification-alert');
    oldNotifications.forEach(n => n.remove());

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

    // Анимация появления
    setTimeout(() => notification.classList.add('show'), 10);

    // Автоудаление через 3 секунды
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Обновление счетчика корзины
function updateCartCounter(count) {
    const cartBadges = document.querySelectorAll('.cart-badge');
    cartBadges.forEach(badge => {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'flex' : 'none';
    });
    console.log('🛒 Счетчик обновлен:', count);
}

// Загрузка счетчика при старте
function loadCartCount() {
    fetch('/cart/count')
        .then(response => {
            if (!response.ok) {
                console.warn('Не удалось загрузить счетчик корзины');
                return {count: 0};
            }
            return response.json();
        })
        .then(data => {
            console.log('📊 Начальное количество товаров:', data);
            updateCartCounter(data.count || 0);
        })
        .catch(error => {
            console.error('❌ Ошибка загрузки счетчика:', error);
            updateCartCounter(0);
        });
}

// Дополнительные функции для удобства
function addDigitalBook(bookId, buttonElement) {
    addToCart(bookId, 'DIGITAL', buttonElement);
}

function addPrintedBook(bookId, buttonElement) {
    addToCart(bookId, 'PRINTED', buttonElement);
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    console.log('📖 Страница книги загружена');

    // Загружаем счетчик корзины
    loadCartCount();

    // Обработчик для кнопок "В корзину" на странице деталей
    document.addEventListener('click', function(e) {
        const addToCartBtn = e.target.closest('.add-to-cart-btn');
        if (addToCartBtn) {
            e.preventDefault();

            // Получаем данные из data-атрибутов
            let bookId = addToCartBtn.getAttribute('data-book-id');
            let bookType = addToCartBtn.getAttribute('data-book-type') || 'DIGITAL';

            // Если нет data-атрибутов, ищем другими способами
            if (!bookId) {
                // Попробуем найти в родительских элементах
                const card = addToCartBtn.closest('.book-details-page');
                if (card) {
                    const hiddenInput = card.querySelector('input[name="bookId"]');
                    if (hiddenInput) bookId = hiddenInput.value;
                }
            }

            // Если всё еще не нашли, используем атрибут onclick
            if (!bookId && addToCartBtn.hasAttribute('onclick')) {
                const onclickText = addToCartBtn.getAttribute('onclick');
                const match = onclickText.match(/(\d+)/);
                if (match) bookId = match[1];
            }

            // Определяем тип книги по классу кнопки
            if (!bookType) {
                if (addToCartBtn.classList.contains('btn-primary') ||
                    addToCartBtn.textContent.includes('Печат') ||
                    addToCartBtn.textContent.includes('Printed')) {
                    bookType = 'PRINTED';
                } else {
                    bookType = 'DIGITAL';
                }
            }

            console.log('🎯 Нажата кнопка:', { bookId, bookType });

            if (bookId) {
                addToCart(bookId, bookType, addToCartBtn);
            } else {
                console.error('❌ Не удалось найти ID книги');
                showNotification('Ошибка: не найден ID книги', 'error');
            }
        }
    });

    // Обработчик для кнопок с прямым вызовом функций
    document.addEventListener('click', function(e) {
        // Кнопки для электронной книги
        const digitalBtn = e.target.closest('.btn-digital, [onclick*="DIGITAL"]');
        if (digitalBtn) {
            e.preventDefault();
            const bookId = digitalBtn.getAttribute('data-book-id') ||
                digitalBtn.closest('[data-book-id]')?.getAttribute('data-book-id');
            if (bookId) {
                addDigitalBook(bookId, digitalBtn);
            }
        }

        // Кнопки для печатной книги
        const printedBtn = e.target.closest('.btn-printed, [onclick*="PRINTED"]');
        if (printedBtn) {
            e.preventDefault();
            const bookId = printedBtn.getAttribute('data-book-id') ||
                printedBtn.closest('[data-book-id]')?.getAttribute('data-book-id');
            if (bookId) {
                addPrintedBook(bookId, printedBtn);
            }
        }
    });

    // Добавляем скрытое поле с bookId для упрощения поиска
    const bookCard = document.querySelector('.book-details-page');
    if (bookCard && !bookCard.querySelector('input[name="bookId"]')) {
        const bookId = bookCard.getAttribute('data-book-id') ||
            bookCard.querySelector('[data-book-id]')?.getAttribute('data-book-id');
        if (bookId) {
            const hiddenInput = document.createElement('input');
            hiddenInput.type = 'hidden';
            hiddenInput.name = 'bookId';
            hiddenInput.value = bookId;
            bookCard.appendChild(hiddenInput);
        }
    }
});

// Экспорт функций для использования в HTML
window.addToCart = addToCart;
window.addDigitalBook = addDigitalBook;
window.addPrintedBook = addPrintedBook;
window.showNotification = showNotification;
window.updateCartCounter = updateCartCounter;