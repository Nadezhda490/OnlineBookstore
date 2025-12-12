// Храним цены за штуку для каждого товара
const itemPrices = new Map();

function initializePrices() {
    document.querySelectorAll('.cart-item').forEach(itemElement => {
        const itemId = itemElement.id.replace('cart-item-', '');
        const pricePerItem = parseFloat(itemElement.getAttribute('data-price-per-item'));
        itemPrices.set(itemId, pricePerItem);
    });
}

function updateQuantity(itemId, change) {
    const quantityInput = document.getElementById('quantity-' + itemId);
    if (!quantityInput) {
        console.error('❌ Не найден элемент quantity-' + itemId);
        return;
    }

    const currentQuantity = parseInt(quantityInput.value) || 1;
    const newQuantity = currentQuantity + change;

    if (newQuantity < 1) {
        removeFromCart(itemId);
        return;
    }

    console.log('🔄 Обновление количества:', itemId, newQuantity);

    // Создаем FormData для отправки
    const formData = new FormData();
    formData.append('itemId', itemId);
    formData.append('quantity', newQuantity);

    fetch('/cart/update', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Обновляем количество в интерфейсе
                quantityInput.value = newQuantity;

                // Обновляем итоговую цену у товара
                updateItemTotalPrice(itemId, newQuantity);

                // Обновляем счетчик в навигации
                updateCartCounter(data.cartItemCount || 0);

                // Показываем уведомление
                showNotification('Количество обновлено', 'success');

                // Пересчитываем общую сумму корзины
                updateCartTotal();
            } else {
                showNotification(data.error || 'Ошибка обновления', 'error');
            }
        })
        .catch(error => {
            console.error('❌ Ошибка:', error);
            showNotification('Ошибка обновления: ' + error.message, 'error');
        });
}

function updateItemTotalPrice(itemId, quantity) {
    const pricePerItem = itemPrices.get(itemId) || 0;
    const totalPrice = Math.round(pricePerItem * quantity);

    // Обновляем отображение итоговой цены у товара
    const totalElement = document.getElementById('total-' + itemId);
    if (totalElement) {
        totalElement.textContent = totalPrice;
    }

    // Обновляем элемент с общей ценой
    const priceTotalElement = document.getElementById('price-total-' + itemId);
    if (priceTotalElement) {
        priceTotalElement.innerHTML = `₽<span id="total-${itemId}">${totalPrice}</span>`;
    }

    console.log(`💰 Товар ${itemId}: ${quantity} × ${pricePerItem} = ${totalPrice}`);
}

function updateCartTotal() {
    let total = 0;
    let itemsCount = 0;

    document.querySelectorAll('.cart-item').forEach(itemElement => {
        const itemId = itemElement.id.replace('cart-item-', '');
        const quantityInput = document.getElementById('quantity-' + itemId);

        if (quantityInput) {
            const quantity = parseInt(quantityInput.value) || 1;
            const pricePerItem = itemPrices.get(itemId) || 0;
            const itemTotal = Math.round(pricePerItem * quantity);

            total += itemTotal;
            itemsCount += quantity;
        }
    });

    console.log('💰 Общая сумма корзины:', total, 'Количество товаров:', itemsCount);

    // Обновляем итоговую сумму
    const cartTotalAmountElement = document.getElementById('cartTotalAmount');
    if (cartTotalAmountElement) {
        cartTotalAmountElement.textContent = Math.round(total);
    }

    // Обновляем стоимость товаров
    const itemsTotalAmountElement = document.getElementById('itemsTotalAmount');
    if (itemsTotalAmountElement) {
        itemsTotalAmountElement.textContent = Math.round(total);
    }

    // Обновляем счетчик товаров
    const itemsCountElement = document.getElementById('itemsCount');
    if (itemsCountElement) {
        itemsCountElement.textContent = itemsCount + ' шт.';
    }
}

function removeFromCart(itemId) {
    if (!confirm('Удалить товар из корзины?')) {
        return;
    }

    console.log('🗑️ Удаление из корзины:', itemId);

    fetch('/cart/remove/' + itemId, {
        method: 'POST'
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const itemElement = document.getElementById('cart-item-' + itemId);
                if (itemElement) {
                    itemElement.remove();
                }

                // Удаляем цену из хранилища
                itemPrices.delete(itemId);

                updateCartCounter(data.cartItemCount || 0);
                showNotification('Товар удален из корзины', 'success');
                updateCartTotal();

                if (data.cartItemCount === 0) {
                    setTimeout(() => location.reload(), 1000);
                }
            } else {
                showNotification(data.error || 'Ошибка удаления', 'error');
            }
        })
        .catch(error => {
            console.error('❌ Ошибка:', error);
            showNotification('Ошибка удаления: ' + error.message, 'error');
        });
}

function clearCart() {
    if (!confirm('Очистить всю корзину?')) {
        return;
    }

    console.log('🧹 Очистка корзины');

    fetch('/cart/clear', {
        method: 'POST'
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showNotification('Корзина очищена', 'success');
                updateCartCounter(0);

                // Очищаем хранилище цен
                itemPrices.clear();

                // Удаляем все элементы корзины
                document.querySelectorAll('.cart-item').forEach(item => {
                    item.remove();
                });

                updateCartTotal();
                setTimeout(() => location.reload(), 1000);
            } else {
                showNotification(data.error || 'Ошибка очистки корзины', 'error');
            }
        })
        .catch(error => {
            console.error('❌ Ошибка:', error);
            showNotification('Ошибка очистки корзины: ' + error.message, 'error');
        });
}

function updateCartCounter(count) {
    const cartBadge = document.getElementById('cartBadge');
    if (cartBadge) {
        cartBadge.textContent = count;
    }
}

function showNotification(message, type = 'success') {
    // Удаляем старые уведомления
    const oldNotifications = document.querySelectorAll('.notification-alert');
    oldNotifications.forEach(n => n.remove());

    // Создаем элемент уведомления
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} notification-alert position-fixed top-0 end-0 m-3`;
    notification.style.zIndex = '1050';
    notification.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="fas fa-${type === 'success' ? 'check' : 'exclamation'}-circle me-2"></i>
                <span>${message}</span>
                <button type="button" class="btn-close ms-auto" onclick="this.parentElement.parentElement.remove()"></button>
            </div>
        `;

    document.body.appendChild(notification);

    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 3000);
}

// Инициализация
document.addEventListener('DOMContentLoaded', function() {
    console.log('🛒 Страница корзины загружена');

    // Инициализируем цены
    initializePrices();

    // Обработчики для кнопок +/-
    document.addEventListener('click', function(e) {
        // Кнопка "+"
        if (e.target.classList.contains('btn-plus') ||
            e.target.closest('.btn-plus')) {
            e.preventDefault();
            const button = e.target.closest('.btn-plus');
            const itemId = button.getAttribute('data-item-id');
            if (itemId) {
                updateQuantity(itemId, 1);
            }
        }

        // Кнопка "-"
        if (e.target.classList.contains('btn-minus') ||
            e.target.closest('.btn-minus')) {
            e.preventDefault();
            const button = e.target.closest('.btn-minus');
            const itemId = button.getAttribute('data-item-id');
            if (itemId) {
                updateQuantity(itemId, -1);
            }
        }

        // Кнопка удаления
        if (e.target.classList.contains('btn-delete') ||
            e.target.closest('.btn-delete')) {
            e.preventDefault();
            const button = e.target.closest('.btn-delete');
            const itemId = button.getAttribute('data-item-id');
            if (itemId) {
                removeFromCart(itemId);
            }
        }
    });

    // Первоначальное вычисление общей суммы
    updateCartTotal();
});

// Экспорт функций
window.updateQuantity = updateQuantity;
window.removeFromCart = removeFromCart;
window.clearCart = clearCart;
window.showNotification = showNotification;
window.updateCartTotal = updateCartTotal;