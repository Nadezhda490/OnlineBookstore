package com.smarttodo.clickbook.service;

import com.smarttodo.clickbook.entity.FAQ;
import com.smarttodo.clickbook.repository.FAQRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.util.*;

@Service
@Transactional
public class FAQService {

    @Autowired
    private FAQRepository faqRepository;

    @PostConstruct
    public void initDefaultFAQs() {
        if (faqRepository.count() == 0) {
            System.out.println("🔄 Создание стандартных вопросов FAQ...");

            List<FAQ> defaultFAQs = Arrays.asList(
                    createFAQ("Как сделать заказ?",
                            "1. Добавьте книги в корзину<br>2. Перейдите в корзину<br>3. Выберите способ доставки<br>4. Оплатите заказ",
                            "Заказы", 1),
                    createFAQ("Какие сроки доставки?",
                            "Почта России: 7-14 дней<br>Яндекс Доставка: 1-3 дня<br>СДЭК: 1-2 дня<br>Электронные книги: мгновенно",
                            "Доставка", 2),
                    createFAQ("Какие способы оплаты доступны?",
                            "Банковские карты (Visa, Mastercard, МИР)<br>Наложенный платеж<br>Все платежи защищены SSL-шифрованием",
                            "Оплата", 3),
                    createFAQ("Как получить электронную книгу?",
                            "После оплаты электронная книга будет доступна в разделе 'Мои заказы'. Вы можете скачать ее в формате PDF или читать онлайн.",
                            "Электронные книги", 4),
                    createFAQ("Как изменить пароль?",
                            "1. Перейдите в раздел 'Профиль'<br>2. Нажмите 'Изменить пароль'<br>3. Введите текущий и новый пароль",
                            "Аккаунт", 5),
                    createFAQ("Как вернуть товар?",
                            "Возврат возможен в течение 30 дней с момента получения при сохранении товарного вида. " +
                                    "Свяжитесь со службой поддержки по email: <strong>support@clickbook.ru</strong>",
                            "Возврат", 6)
            );

            faqRepository.saveAll(defaultFAQs);
            System.out.println("✅ Стандартные вопросы FAQ созданы!");
        }
    }

    private FAQ createFAQ(String question, String answer, String category, int order) {
        FAQ faq = new FAQ(question, answer, category);
        faq.setDisplayOrder(order);
        return faq;
    }

    public List<FAQ> getAllFAQs() {
        try {
            List<FAQ> allFaqs = faqRepository.findAllByOrderByDisplayOrderAsc();

            // Убираем дубликаты
            Map<Long, FAQ> uniqueMap = new LinkedHashMap<>();
            for (FAQ faq : allFaqs) {
                uniqueMap.put(faq.getId(), faq);
            }
            return new ArrayList<>(uniqueMap.values());
        } catch (Exception e) {
            System.err.println("Ошибка получения FAQ: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public FAQ getFAQById(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ не найден с ID: " + id));
    }

    public FAQ saveFAQ(FAQ faq) {
        if (faq.getQuestion() == null || faq.getQuestion().trim().isEmpty()) {
            throw new RuntimeException("Вопрос не может быть пустым");
        }
        if (faq.getAnswer() == null || faq.getAnswer().trim().isEmpty()) {
            throw new RuntimeException("Ответ не может быть пустым");
        }
        if (faq.getCategory() == null || faq.getCategory().trim().isEmpty()) {
            throw new RuntimeException("Категория не может быть пустой");
        }

        return faqRepository.save(faq);
    }

    public void deleteFAQ(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new RuntimeException("FAQ с ID " + id + " не найден");
        }
        faqRepository.deleteById(id);
    }

    public List<FAQ> getActiveFAQs() {
        // Возвращаем все FAQ
        return faqRepository.findAllByOrderByDisplayOrderAsc();
    }

    public List<FAQ> getFAQsByCategory(String category) {
        return faqRepository.findByCategoryOrderByDisplayOrderAsc(category);
    }

    public List<String> getDistinctCategories() {
        return faqRepository.findDistinctCategories();
    }
}