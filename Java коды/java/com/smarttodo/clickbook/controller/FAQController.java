package com.smarttodo.clickbook.controller;

import com.smarttodo.clickbook.entity.FAQ;
import com.smarttodo.clickbook.service.FAQService;
import com.smarttodo.clickbook.service.UserSessionService;
import com.smarttodo.clickbook.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/store")
public class FAQController {

    @Autowired
    private FAQService faqService;

    @Autowired
    private UserSessionService userSessionService;

    @GetMapping("/faq")
    public String faqPage(Model model, HttpSession session) {
        if (!userSessionService.isLoggedIn(session)) {
            return "redirect:/";
        }

        var user = userSessionService.getCurrentUser(session);
        model.addAttribute("user", user);

        // Получаем FAQ по категориям
        List<String> categories = faqService.getDistinctCategories();
        model.addAttribute("categories", categories);

        for (String category : categories) {
            List<FAQ> categoryFAQs = faqService.getFAQsByCategory(category);
            model.addAttribute(category.toLowerCase().replace(" ", "") + "FAQs", categoryFAQs);
        }

        // Все FAQ
        List<FAQ> allFAQs = faqService.getAllFAQs();
        model.addAttribute("allFAQs", allFAQs);

        return "faq";
    }
}