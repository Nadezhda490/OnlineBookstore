package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.entity.*;
import com.smarttodo.clickbook.repository.CategoryRepository;
import com.smarttodo.clickbook.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private FAQService faqService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CategoryRepository categoryRepository;

    // Проверка прав администратора
    private boolean isAdmin(HttpSession session) {
        User currentUser = userSessionService.getCurrentUser(session);
        return currentUser != null && "ROLE_ADMIN".equals(currentUser.getRole());
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        User currentUser = userSessionService.getCurrentUser(session);
        model.addAttribute("user", currentUser);

        model.addAttribute("totalBooks", bookService.getTotalBooksCount());
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalOrders", orderService.getAllOrders().size());
        model.addAttribute("totalFAQs", faqService.getAllFAQs().size());

        return "dashboard";
    }

    // Управление книгами

    // Список книг
    @GetMapping("/books")
    public String bookManagement(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        model.addAttribute("categories", bookService.getAllCategories());

        return "/admin/books-admin";
    }

    // Форма добавления книги
    @GetMapping("/books/new")
    public String showAddBookForm(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("book", new Book());
        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("ageRatings", Arrays.asList("0+", "6+", "12+", "16+", "18+"));

        return "admin/book-add-form";
    }

    // Сохранение новой книги
    @PostMapping("/books/save")
    public String saveBook(@ModelAttribute Book book,
                           @RequestParam(required = false) Long categoryId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        try {
            // Автоматическое дефолтное изображение
            if (book.getImageUrl() == null || book.getImageUrl().trim().isEmpty()) {
                System.out.println("📸 Админ: устанавливаем дефолтное изображение для новой книги");
                book.setImageUrl("/images/books/default.jpg");
            }

            // Если категория не выбрана - устанавливаем null
            if (categoryId != null) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Категория не найдена"));
                book.setCategory(category);
            } else {
                book.setCategory(null);
            }

            bookService.saveBook(book);
            redirectAttributes.addFlashAttribute("success", "Книга успешно добавлена");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении книги: " + e.getMessage());
        }

        return "redirect:/admin/books";
    }

    // Форма редактирования книги
    @GetMapping("/books/{id}/edit")
    public String editBookForm(@PathVariable Long id,
                               Model model,
                               HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("ageRatings", Arrays.asList("0+", "6+", "12+", "16+", "18+"));

        return "admin/book-edit-form";
    }

    // Обновление книги
    @PostMapping("/books/{id}/update")
    public String updateBook(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String author,
                             @RequestParam String description,
                             @RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) String genre,
                             @RequestParam(required = false) String ageRating,
                             @RequestParam Double priceDigital,
                             @RequestParam Double pricePrinted,
                             @RequestParam(required = false) Integer pages,
                             @RequestParam(required = false) String isbn,
                             @RequestParam(required = false) String imageUrl,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        try {
            Book book = bookService.getBookById(id);

            // Обновляем все поля
            book.setTitle(title);
            book.setAuthor(author);
            book.setDescription(description);

            if (categoryId != null) {
                // Находим категорию по ID
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Категория не найдена"));
                book.setCategory(category);
            }

            book.setGenre(genre);
            book.setAgeRating(ageRating != null ? ageRating : "0+");
            book.setPriceDigital(priceDigital);
            book.setPricePrinted(pricePrinted);
            book.setPages(pages);

            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                book.setImageUrl(imageUrl);
            }

            // Автоматическое дефолтное изображение
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                System.out.println("📸 Админ: устанавливаем дефолтное изображение при обновлении книги ID: " + id);
                book.setImageUrl("/images/books/default.jpg");
            } else {
                book.setImageUrl(imageUrl);
            }

            bookService.saveBook(book);
            redirectAttributes.addFlashAttribute("success", "Книга успешно обновлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении книги: " + e.getMessage());
        }
        return "redirect:/admin/books";
    }

    // Удаление книги
    @GetMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        try {
            // Удаляем книгу
            bookService.deleteBook(id);
            System.out.println("✅ Книга удалена: " + id);
            redirectAttributes.addFlashAttribute("success", "Книга успешно удалена");

        } catch (Exception e) {
            System.err.println("❌ Ошибка удаления: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "redirect:/admin/books";
    }

    // Управление заказами
    @GetMapping("/order")
    public String orderManagement(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        User currentUser = userSessionService.getCurrentUser(session);
        model.addAttribute("user", currentUser);

        // Получаем все заказы
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);

        return "admin/order";
    }

    // Управление FAQ
    @PostMapping("/faq/save")
    public String saveFAQ(@ModelAttribute FAQ faq,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        try {
            faqService.saveFAQ(faq);
            redirectAttributes.addFlashAttribute("success", "Вопрос успешно сохранен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при сохранении: " + e.getMessage());
        }
        return "redirect:/admin/faq";
    }

    // Форма редактирования FAQ
    @GetMapping("/faq/{id}/edit")
    public String editFAQForm(@PathVariable Long id,
                              Model model,
                              HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        FAQ faq = faqService.getFAQById(id);
        model.addAttribute("faq", faq);
        model.addAttribute("categories", Arrays.asList(
                "Заказы", "Доставка", "Оплата", "Электронные книги", "Аккаунт", "Возврат"
        ));
        return "admin/faq-form";
    }

    @PostMapping("/faq/{id}/delete")
    public String deleteFAQ(@PathVariable Long id,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        try {
            faqService.deleteFAQ(id);
            redirectAttributes.addFlashAttribute("success", "Вопрос успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/admin/faq";
    }

    // Управление FAQ
    @GetMapping("/faq")
    public String faqManagement(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        List<FAQ> faqs = faqService.getAllFAQs();
        model.addAttribute("faqs", faqs);
        model.addAttribute("categories", Arrays.asList(
                "Заказы", "Доставка", "Оплата", "Электронные книги", "Аккаунт", "Возврат"
        ));
        return "admin/faq-admin";
    }

    @GetMapping("/faq/new")
    public String showAddFAQForm(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("faq", new FAQ());
        model.addAttribute("categories", Arrays.asList(
                "Заказы", "Доставка", "Оплата", "Электронные книги", "Аккаунт", "Возврат"
        ));

        return "admin/faq-form";
    }

    @PostMapping("/faq/update")
    public String updateFAQ(@ModelAttribute FAQ faq,
                            @RequestParam Long id,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        try {
            FAQ existingFAQ = faqService.getFAQById(id);
            existingFAQ.setQuestion(faq.getQuestion());
            existingFAQ.setAnswer(faq.getAnswer());
            existingFAQ.setCategory(faq.getCategory());
            existingFAQ.setDisplayOrder(faq.getDisplayOrder());

            faqService.saveFAQ(existingFAQ);
            redirectAttributes.addFlashAttribute("success", "Вопрос успешно обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении: " + e.getMessage());
        }
        return "redirect:/admin/faq";
    }

    @GetMapping("/faq/{id}/toggle")
    public String toggleFAQStatus(@PathVariable Long id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        try {
            redirectAttributes.addFlashAttribute("success", "Статус обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/faq";
    }
}