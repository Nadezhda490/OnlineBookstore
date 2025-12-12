package com.smarttodo.clickbook.service;

import com.smarttodo.clickbook.entity.Book;
import com.smarttodo.clickbook.entity.Category;
import com.smarttodo.clickbook.repository.BookRepository;
import com.smarttodo.clickbook.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // ================== ОСНОВНЫЕ МЕТОДЫ ==================

    /**
     * Получить все книги
     */
    public List<Book> getAllBooks() {
        try {
            List<Book> books = bookRepository.findAll();
            return books != null ? books : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Ошибка при получении всех книг: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Получить книгу по ID
     */
    public Book getBookById(Long id) {
        try {
            if (id == null) {
                throw new RuntimeException("ID книги не может быть null");
            }

            Optional<Book> book = bookRepository.findById(id);
            if (book.isPresent()) {
                return book.get();
            } else {
                throw new RuntimeException("Книга не найдена с ID: " + id);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Ошибка при получении книги по ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Внутренняя ошибка при получении книги");
        }
    }

    /**
     * Сохранить книгу (создание или обновление)
     */
    public Book saveBook(Book book) {
        try {
            // Валидация
            if (book == null) {
                throw new RuntimeException("Книга не может быть null");
            }

            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                throw new RuntimeException("Название книги обязательно");
            }

            if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
                throw new RuntimeException("Автор книги обязателен");
            }

            if (book.getPriceDigital() == null || book.getPriceDigital() < 0) {
                throw new RuntimeException("Цена электронной версии должна быть положительной");
            }

            if (book.getPricePrinted() == null || book.getPricePrinted() < 0) {
                throw new RuntimeException("Цена печатной версии должна быть положительной");
            }

            // Если категория null, но есть categoryId - обрабатываем
            if (book.getCategory() != null && book.getCategory().getId() != null) {
                // Проверяем, существует ли категория в БД
                Optional<Category> existingCategory = categoryRepository.findById(book.getCategory().getId());
                if (existingCategory.isPresent()) {
                    book.setCategory(existingCategory.get());
                } else {
                    // Если категория не найдена - устанавливаем null
                    book.setCategory(null);
                }
            }

            // Автоматическая подстановка дефолтного изображения
            if (book.getImageUrl() == null || book.getImageUrl().trim().isEmpty()) {
                System.out.println("📸 Устанавливаем дефолтное изображение для книги: " + book.getTitle());
                book.setImageUrl("/images/books/default.jpg");
            }

            // Если путь начинается с /static/, убираем его
            if (book.getImageUrl().startsWith("/static/")) {
                book.setImageUrl(book.getImageUrl().substring(7));
            }

            // Сохраняем книгу
            Book savedBook = bookRepository.save(book);
            return savedBook;

        } catch (Exception e) {
            System.err.println("❌ Ошибка сохранения книги: " + e.getMessage());
            throw new RuntimeException("Ошибка сохранения книги: " + e.getMessage(), e);
        }
    }

    /**
     * Удалить книгу по ID
     */
    public void deleteBook(Long id) {
        try {
            System.out.println("🔄 Удаление книги ID: " + id);

            if (id == null) {
                throw new RuntimeException("ID книги не может быть null");
            }

            // Проверяем существование книги
            Book book = bookRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Книга с ID " + id + " не найдена"));

            System.out.println("📖 Удаляемая книга: " + book.getTitle());

            // Удаляем книгу
            bookRepository.delete(book);

            System.out.println("✅ Книга ID " + id + " удалена из БД");

        } catch (Exception e) {
            System.err.println("❌ Ошибка удаления книги: " + e.getMessage());
            throw new RuntimeException("Ошибка удаления книги: " + e.getMessage(), e);
        }
    }

    // ================== РЕКОМЕНДОВАННЫЕ КНИГИ ==================

    /**
     * Получить случайные рекомендованные книги
     */
    public List<Book> getRandomRecommendedBooks(int count) {
        try {
            List<Book> allBooks = getAllBooks();

            if (allBooks.isEmpty()) {
                return new ArrayList<>();
            }

            // Создаем копию списка для перемешивания
            List<Book> shuffledBooks = new ArrayList<>(allBooks);

            // Перемешиваем список книг
            Collections.shuffle(shuffledBooks);

            // Берем первые N книг (или меньше, если книг мало)
            int booksToReturn = Math.min(count, shuffledBooks.size());
            return shuffledBooks.subList(0, booksToReturn);

        } catch (Exception e) {
            System.err.println("❌ Ошибка получения случайных книг: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Получить рекомендованные книги (6 случайных)
     */
    public List<Book> getRecommendedBooks() {
        return getRandomRecommendedBooks(6);
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Получить все категории
     */
    public List<Category> getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            return categories != null ? categories : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения категорий: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Получить книги для несовершеннолетних
     */
    public List<Book> getBooksForMinors() {
        try {
            List<Book> books = bookRepository.findBooksForMinors();
            return books != null ? books : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения книг для несовершеннолетних: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Получить количество всех книг
     */
    public long getTotalBooksCount() {
        try {
            return bookRepository.count();
        } catch (Exception e) {
            System.err.println("❌ Ошибка подсчета книг: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Получить книги по списку ID
     */
    public List<Book> getBooksByIds(List<Long> ids) {
        try {
            List<Book> books = new ArrayList<>();
            for (Long id : ids) {
                Optional<Book> book = bookRepository.findById(id);
                book.ifPresent(books::add);
            }
            return books;
        } catch (Exception e) {
            System.err.println("❌ Ошибка получения книг по ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Проверить существование книги по ID
     */
    public boolean existsById(Long id) {
        try {
            return bookRepository.existsById(id);
        } catch (Exception e) {
            System.err.println("❌ Ошибка проверки существования книги: " + e.getMessage());
            return false;
        }
    }

    /**
     * Инициализация тестовых данных
     */
    @PostConstruct
    public void initTestData() {
        try {
            if (bookRepository.count() == 0) {
                System.out.println("🔄 База данных книг пуста, создание тестовых данных...");
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка инициализации тестовых данных: " + e.getMessage());
        }
    }

    /**
     * Поиск книг по запросу
     */
    public List<Book> searchBooks(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return getAllBooks();
            }

            String searchQuery = query.trim();
            List<Book> results = bookRepository.searchBooks(searchQuery);

            return results;

        } catch (Exception e) {
            System.err.println("❌ Ошибка поиска книг: " + e.getMessage());
            return getAllBooks();
        }
    }
}