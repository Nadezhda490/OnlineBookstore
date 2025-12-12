package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.entity.Book;
import com.smarttodo.clickbook.entity.User;
import com.smarttodo.clickbook.service.BookService;
import com.smarttodo.clickbook.service.CartService;
import com.smarttodo.clickbook.service.UserSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/store")
public class BookStoreController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private CartService cartService;

    @GetMapping("/books")
    public String booksPage(Model model, HttpSession session) {
        if (!userSessionService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = userSessionService.getCurrentUser(session);
        List<Book> books = bookService.getAllBooks();

        for (int i = 0; i < Math.min(3, books.size()); i++) {
            Book book = books.get(i);
        }

        model.addAttribute("user", user);
        model.addAttribute("books", books);
        model.addAttribute("cartItemCount", cartService.getCartItemCount(user.getUsername()));

        return "books";
    }

    @GetMapping("/book/{id}")
    public String bookDetails(@PathVariable Long id, Model model, HttpSession session) {
        if (!userSessionService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = userSessionService.getCurrentUser(session);
        Book book = bookService.getBookById(id);

        model.addAttribute("user", user);
        model.addAttribute("book", book);
        model.addAttribute("cartItemCount", cartService.getCartItemCount(user.getUsername()));

        // Счетчик корзины только для пользователей
        if (user != null && !"ROLE_ADMIN".equals(user.getRole())) {
            model.addAttribute("cartItemCount", cartService.getCartItemCount(user.getUsername()));
        } else {
            model.addAttribute("cartItemCount", 0); // Для админов 0
        }

        return "book-details";
    }

    // Поиск книг в магазине
    @GetMapping("/search")
    public String searchBooks(@RequestParam(name = "query", required = false) String query,
                              Model model, HttpSession session) {

        if (!userSessionService.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = userSessionService.getCurrentUser(session);
        List<Book> books;
        String searchTitle;

        if (query != null && !query.trim().isEmpty()) {
            books = bookService.searchBooks(query);
            searchTitle = "Результаты поиска: '" + query + "'";
        } else {
            books = bookService.getAllBooks();
            searchTitle = "Каталог книг";
        }

        model.addAttribute("user", user);
        model.addAttribute("books", books);
        model.addAttribute("searchQuery", query != null ? query : "");
        model.addAttribute("searchTitle", searchTitle);
        model.addAttribute("resultsCount", books.size());
        model.addAttribute("cartItemCount", cartService.getCartItemCount(user.getUsername()));

        return "books";
    }
}