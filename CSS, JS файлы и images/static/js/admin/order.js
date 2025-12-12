// Обработчик для кнопки "Подробнее"
document.addEventListener('DOMContentLoaded', function() {
    const orderDetailsModal = document.getElementById('orderDetailsModal');

    if (orderDetailsModal) {
        orderDetailsModal.addEventListener('show.bs.modal', function(event) {
            const button = event.relatedTarget;
            const orderId = button.getAttribute('data-order-id');

            document.getElementById('modalOrderId').textContent = orderId;

            // Временно показываем заглушку
            document.getElementById('modalOrderDetails').innerHTML = `
                    <div class="text-center py-4">
                        <i class="fas fa-spinner fa-spin fa-2x mb-3"></i>
                        <p>Загрузка деталей заказа...</p>
                    </div>
                `;
        });
    }
});