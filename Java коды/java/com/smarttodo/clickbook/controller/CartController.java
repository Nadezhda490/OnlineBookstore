package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.service.CartService;
import com.smarttodo.clickbook.service.UserSessionService;
import com.smarttodo.clickbook.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserSessionService userSessionService;

    @GetMapping
    public String showCart(Model model, HttpSession session) {
        User currentUser = userSessionService.getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        String username = currentUser.getUsername();
        var cartItems = cartService.getCartItems(username);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("user", currentUser);
        model.addAttribute("cartItemCount", cartItems.size());

        double total = 0.0;
        for (var item : cartItems) {
            double price = "DIGITAL".equals(item.getBookType()) ?
                    item.getBook().getPriceDigital() :
                    item.getBook().getPricePrinted();
            total += price * item.getQuantity();
        }
        model.addAttribute("cartTotal", total);

        return "cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestParam Long bookId,
                                         @RequestParam(defaultValue = "DIGITAL") String bookType,
                                         HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = userSessionService.getCurrentUser(session);
            if (currentUser == null) {
                response.put("success", false);
                response.put("error", "Требуется авторизация");
                return response;
            }

            cartService.addToCart(bookId, currentUser.getUsername(), bookType);
            Integer count = cartService.getCartItemCount(currentUser.getUsername());

            response.put("success", true);
            response.put("message", "Книга добавлена в корзину");
            response.put("count", count);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateQuantity(@RequestParam Long itemId,
                                              @RequestParam Integer quantity,
                                              HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = userSessionService.getCurrentUser(session);
            if (currentUser == null) {
                response.put("success", false);
                response.put("error", "Требуется авторизация");
                return response;
            }

            cartService.updateQuantity(itemId, quantity, currentUser.getUsername());
            Integer count = cartService.getCartItemCount(currentUser.getUsername());

            response.put("success", true);
            response.put("cartItemCount", count);
            response.put("message", "Количество обновлено");

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/remove/{itemId}")
    @ResponseBody
    public Map<String, Object> removeFromCart(@PathVariable Long itemId,
                                              HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = userSessionService.getCurrentUser(session);
            if (currentUser == null) {
                response.put("success", false);
                response.put("error", "Требуется авторизация");
                return response;
            }

            cartService.removeFromCart(itemId, currentUser.getUsername());
            Integer count = cartService.getCartItemCount(currentUser.getUsername());

            response.put("success", true);
            response.put("cartItemCount", count);
            response.put("message", "Товар удален из корзины");

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/clear")
    @ResponseBody
    public Map<String, Object> clearCart(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = userSessionService.getCurrentUser(session);
            if (currentUser == null) {
                response.put("success", false);
                response.put("error", "Требуется авторизация");
                return response;
            }

            cartService.clearCart(currentUser.getUsername());

            response.put("success", true);
            response.put("cartItemCount", 0);
            response.put("message", "Корзина очищена");

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @GetMapping("/count")
    @ResponseBody
    public Map<String, Object> getCartCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = userSessionService.getCurrentUser(session);
            if (currentUser == null) {
                response.put("count", 0);
            } else {
                Integer count = cartService.getCartItemCount(currentUser.getUsername());
                response.put("count", count != null ? count : 0);
            }
        } catch (Exception e) {
            response.put("count", 0);
        }

        return response;
    }
}