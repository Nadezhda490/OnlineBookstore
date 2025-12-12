package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.service.CartService;
import com.smarttodo.clickbook.service.OrderService;
import com.smarttodo.clickbook.service.UserSessionService;
import com.smarttodo.clickbook.entity.User;
import com.smarttodo.clickbook.entity.Order;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserSessionService userSessionService;

    @PostMapping("/process")
    public String processPayment(@RequestParam String deliveryCity,
                                 @RequestParam String deliveryService,
                                 HttpSession session,
                                 Model model) {
        User currentUser = userSessionService.getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        String username = currentUser.getUsername();

        try {
            // Проверяем, есть ли товары в корзине
            var cartItems = cartService.getCartItems(username);
            if (cartItems == null || cartItems.isEmpty()) {
                model.addAttribute("error", "Корзина пуста");
                return paymentPage(model, session);
            }

            // Проверка заполнения полей
            if (deliveryCity == null || deliveryCity.trim().isEmpty()) {
                model.addAttribute("error", "Укажите город доставки");
                return paymentPage(model, session);
            }

            if (deliveryService == null || deliveryService.trim().isEmpty()) {
                model.addAttribute("error", "Выберите способ доставки");
                return paymentPage(model, session);
            }

            // Рассчитываем сумму корзины
            double cartTotal = 0.0;
            for (var item : cartItems) {
                double price = "DIGITAL".equals(item.getBookType()) ?
                        item.getBook().getPriceDigital() :
                        item.getBook().getPricePrinted();
                cartTotal += price * item.getQuantity();
            }

            // Создаем заказ из корзины
            Order order = orderService.createOrder(username, deliveryCity, deliveryService);

            // Очищаем корзину
            cartService.clearCart(username);

            // Добавляем данные в модель для подтверждения
            model.addAttribute("success", "Вы успешно оплатили товар!");
            model.addAttribute("orderId", order.getId());
            model.addAttribute("deliveryCity", deliveryCity);
            model.addAttribute("deliveryService", deliveryService);
            model.addAttribute("cartTotal", cartTotal);
            model.addAttribute("cartItemCount", 0); // Корзина очищена

            return "payment-success";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при оформлении заказа: " + e.getMessage());
            return paymentPage(model, session);
        }
    }

    @GetMapping
    public String paymentPage(Model model, HttpSession session) {
        User currentUser = userSessionService.getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        String username = currentUser.getUsername();

        // Получаем товары из корзины
        var cartItems = cartService.getCartItems(username);
        if (cartItems == null || cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        // Рассчитываем сумму
        double cartTotal = 0.0;
        for (var item : cartItems) {
            double price = "DIGITAL".equals(item.getBookType()) ?
                    item.getBook().getPriceDigital() :
                    item.getBook().getPricePrinted();
            cartTotal += price * item.getQuantity();
        }

        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("totalAmount", cartTotal);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartItemCount", cartItems.size());

        // Список служб доставки
        List<String> deliveryServices = Arrays.asList("Почта России", "Яндекс Доставка", "СДЭК");
        model.addAttribute("deliveryServices", deliveryServices);

        return "payment";
    }
}