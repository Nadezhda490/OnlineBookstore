// Анимация появления элементов при скролле
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.animation = `fadeInUp 0.6s ease ${entry.target.dataset.delay || '0s'} both`;
            observer.unobserve(entry.target);
        }
    });
}, observerOptions);

// Наблюдаем за карточками книг
document.addEventListener('DOMContentLoaded', function() {
    const bookCards = document.querySelectorAll('.book-card');
    bookCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.dataset.delay = `${index * 0.1}s`;
        observer.observe(card);
    });

    // Анимация кнопок
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach(btn => {
        btn.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
        });

        btn.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });

    // Анимация добавления в корзину
    const addToCartForms = document.querySelectorAll('form[action*="/cart/add"]');
    addToCartForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const button = this.querySelector('button[type="submit"]');
            const originalText = button.innerHTML;

            button.innerHTML = '<span class="loading"></span> Добавляем...';
            button.disabled = true;

            setTimeout(() => {
                button.innerHTML = '✓ Добавлено!';
                button.classList.remove('btn-primary');
                button.classList.add('btn-success');

                setTimeout(() => {
                    button.innerHTML = originalText;
                    button.disabled = false;
                    button.classList.remove('btn-success');
                    button.classList.add('btn-primary');
                }, 2000);
            }, 800);
        });
    });
});

// Параллакс эффект для герой секции
window.addEventListener('scroll', function() {
    const scrolled = window.pageYOffset;
    const hero = document.querySelector('.hero-section');
    if (hero) {
        hero.style.transform = `translateY(${scrolled * 0.5}px)`;
    }
});