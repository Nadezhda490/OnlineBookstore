package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.entity.Book;
import com.smarttodo.clickbook.entity.User;
import com.smarttodo.clickbook.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserSessionService userSessionService;

    @GetMapping
    public String showBooks(Model model, HttpSession session) {
        model.addAttribute("seoTitle", "Каталог книг - КликБук");
        model.addAttribute("seoDescription", "Каталог книг магазина КликБук. Художественная литература, фантастика, детективы, триллеры.");
        model.addAttribute("books", bookService.getAllBooks());

        User currentUser = userSessionService.getCurrentUser(session);
        if (currentUser != null) {
            model.addAttribute("cartItemCount", cartService.getCartItemCount(currentUser.getUsername()));
        } else {
            model.addAttribute("cartItemCount", 0);
        }
        return "books";
    }

    @GetMapping("/{id}")
    public String showBook(@PathVariable Long id, Model model, HttpSession session) {
        Book book = bookService.getBookById(id);

        // ЛОГИРОВАНИЕ для диагностики
        System.out.println("=".repeat(50));
        System.out.println("📖 КНИГА ДЕТАЛИ:");
        System.out.println("ID: " + book.getId());
        System.out.println("Название: " + book.getTitle());
        System.out.println("Автор: " + book.getAuthor());
        System.out.println("Путь к изображению из БД: " + book.getImageUrl());
        System.out.println("Полный URL: http://localhost:8080" + book.getImageUrl());
        System.out.println("=".repeat(50));

        model.addAttribute("book", book);

        User currentUser = userSessionService.getCurrentUser(session);
        if (currentUser != null) {
            String username = currentUser.getUsername();
            model.addAttribute("cartItemCount", cartService.getCartItemCount(username));
        } else {
            model.addAttribute("cartItemCount", 0);
        }

        return "book-details";
    }

    // Поиск книг
    @GetMapping("/search")
    public String searchBooks(@RequestParam(name = "query", required = false) String query,
                              Model model, HttpSession session) {

        System.out.println("🔍 Получен запрос на поиск: '" + query + "'");

        List<Book> books;
        String searchTitle;

        if (query != null && !query.trim().isEmpty()) {
            books = bookService.searchBooks(query);
            searchTitle = "Результаты поиска: '" + query + "'";
        } else {
            books = bookService.getAllBooks();
            searchTitle = "Каталог книг";
        }

        model.addAttribute("seoTitle", searchTitle + " - КликБук");
        model.addAttribute("seoDescription", "Поиск книг в магазине КликБук");
        model.addAttribute("books", books);
        model.addAttribute("searchQuery", query != null ? query : "");
        model.addAttribute("searchTitle", searchTitle);
        model.addAttribute("resultsCount", books.size());

        User currentUser = userSessionService.getCurrentUser(session);
        if (currentUser != null) {
            model.addAttribute("cartItemCount", cartService.getCartItemCount(currentUser.getUsername()));
        } else {
            model.addAttribute("cartItemCount", 0);
        }

        return "books";
    }

    @GetMapping("/debug")
    @ResponseBody
    public String debugBooks() {
        List<Book> books = bookService.getAllBooks();
        StringBuilder result = new StringBuilder();
        result.append("<h1>Отладка книг</h1>");
        result.append("<table border='1'><tr><th>ID</th><th>Название</th><th>Путь в БД</th><th>Используемый путь</th><th>Тест</th></tr>");

        for (Book book : books) {
            String dbPath = book.getImageUrl(); // Путь из БД
            String usedPath = book.getImageUrl(); // Путь после преобразования

            result.append("<tr>")
                    .append("<td>").append(book.getId()).append("</td>")
                    .append("<td>").append(book.getTitle()).append("</td>")
                    .append("<td>").append(book.getImageUrl()).append("</td>")
                    .append("<td>").append(usedPath).append("</td>")
                    .append("<td><a href='").append(usedPath)
                    .append("' target='_blank'>Проверить</a></td>")
                    .append("</tr>");
        }

        result.append("</table>");
        return result.toString();
    }

    @GetMapping("/debug/images")
    @ResponseBody
    public String debugImages() {
        List<Book> books = bookService.getAllBooks();
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>ДИАГНОСТИКА ИЗОБРАЖЕНИЙ</h1>");
        sb.append("<table border='1'>");
        sb.append("<tr><th>ID</th><th>Название</th><th>Путь в БД</th><th>Тест URL</th><th>Статус</th></tr>");

        for (Book book : books) {
            String dbPath = book.getImageUrl();
            String testUrl = "http://localhost:8080" + dbPath;
            sb.append("<tr>")
                    .append("<td>").append(book.getId()).append("</td>")
                    .append("<td>").append(book.getTitle()).append("</td>")
                    .append("<td>").append(dbPath).append("</td>")
                    .append("<td><a href='").append(testUrl).append("' target='_blank'>").append(testUrl).append("</a></td>")
                    .append("<td>").append(dbPath != null && dbPath.contains("/images/") ? "✅ OK" : "❌ Проблема").append("</td>")
                    .append("</tr>");
        }

        sb.append("</table>");
        sb.append("<br><a href='/store/books'>Вернуться к книгам</a>");
        return sb.toString();
    }
}