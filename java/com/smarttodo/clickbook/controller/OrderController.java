package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.service.OrderService;
import com.smarttodo.clickbook.service.UserSessionService;
import com.smarttodo.clickbook.entity.User;
import com.smarttodo.clickbook.entity.Order;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/books/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserSessionService userSessionService;

    @GetMapping
    public String userOrders(Model model, HttpSession session) {
        User currentUser = userSessionService.getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        String username = currentUser.getUsername();

        List<Order> orders = orderService.getUserOrders(username);

        model.addAttribute("orders", orders);
        return "orders";
    }
}