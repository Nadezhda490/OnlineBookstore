// Функции для страницы оплаты

// Выбор службы доставки
function selectDeliveryService(serviceElement) {
    const service = serviceElement.getAttribute('data-service');

    // Снимаем выделение
    document.querySelectorAll('.delivery-service-card').forEach(card => {
        card.classList.remove('selected');
    });

    // Выделяем выбранную
    serviceElement.classList.add('selected');

    // Устанавливаем значение в скрытом поле
    document.getElementById('deliveryService').value = service;

    // Рассчитываем стоимость доставки
    calculateDeliveryCost(service);
}

// Расчет стоимости доставки
function calculateDeliveryCost(service) {
    let cost = 0;

    switch(service) {
        case 'Почта России':
            cost = 250;
            break;
        case 'Яндекс Доставка':
            cost = 350;
            break;
        case 'СДЭК':
            cost = 500;
            break;
        default:
            cost = 0;
    }

    // Обновляем отображение стоимости доставки
    const deliveryCostElement = document.getElementById('deliveryCost');
    if (deliveryCostElement) {
        deliveryCostElement.textContent = '₽' + cost;
    }

    // Обновляем итоговую сумму
    updateTotalAmount(cost);
}

// Обновление итоговой суммы
function updateTotalAmount(deliveryCost) {
    const orderTotalElement = document.getElementById('orderTotal');
    const totalAmountElement = document.getElementById('totalAmount');

    if (orderTotalElement && totalAmountElement) {
        const orderTotal = parseFloat(orderTotalElement.textContent) || 0;
        const totalAmount = orderTotal + deliveryCost;

        const totalSpan = totalAmountElement.querySelector('span');
        if (totalSpan) {
            totalSpan.textContent = Math.round(totalAmount);
        }
    }
}

// Валидация формы оплаты
function validatePaymentForm(event) {
    event.preventDefault();

    const deliveryCity = document.getElementById('deliveryCity').value.trim();
    const deliveryService = document.getElementById('deliveryService').value;

    // Проверка города
    if (!deliveryCity) {
        showNotification('Пожалуйста, укажите город доставки', 'error');
        document.getElementById('deliveryCity').focus();
        return false;
    }

    // Проверка службы доставки
    if (!deliveryService) {
        showNotification('Пожалуйста, выберите способ доставки', 'error');
        return false;
    }

    // Отправка формы
    const submitButton = document.getElementById('submitPaymentButton');
    if (submitButton) {
        submitButton.disabled = true;
        submitButton.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Обработка...';
    }

    // Отправляем форму
    document.getElementById('paymentForm').submit();

    return true;
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

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    // Обработчики для карточек доставки
    document.querySelectorAll('.delivery-service-card').forEach(card => {
        card.addEventListener('click', function() {
            selectDeliveryService(this);
        });
    });

    // Обработчик для формы
    const paymentForm = document.getElementById('paymentForm');
    if (paymentForm) {
        paymentForm.addEventListener('submit', validatePaymentForm);
    }

    // Автоматически выбираем первую службу доставки
    const firstServiceCard = document.querySelector('.delivery-service-card');
    if (firstServiceCard) {
        selectDeliveryService(firstServiceCard);
    }
});

// Экспорт функций
window.selectDeliveryService = selectDeliveryService;
window.validatePaymentForm = validatePaymentForm;
window.showNotification = showNotification;