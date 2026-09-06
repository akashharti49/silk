package com.gld.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import com.gld.dto.Order;
import com.gld.service.implentation.OrderService;

import java.nio.charset.StandardCharsets;

/**
 * Customer facing "My Orders" screen: look an order up by the email used
 * while checking out, and cancel it from there.
 */
@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    // =========================================================
    // "MY ORDERS" LOOKUP PAGE
    // =========================================================

    @GetMapping("/my-orders")
    public String myOrders(@RequestParam(required = false) String email, Model model) {

        List<Order> orders = List.of();

        if (email != null && !email.trim().isEmpty()) {
            orders = orderService.getOrdersByEmail(email.trim());
        }

        model.addAttribute("email", email);
        model.addAttribute("orders", orders);
        model.addAttribute("searched", email != null && !email.trim().isEmpty());

        return "my-orders";
    }

    @PostMapping("/my-orders/lookup")
    public String lookupOrders(@RequestParam String email) {
        return "redirect:/my-orders?email=" + UriUtils.encode(email.trim(), StandardCharsets.UTF_8);
    }

    // =========================================================
    // CANCEL AN ORDER (used from "My Orders" and the order-success page)
    // =========================================================

    @PostMapping("/my-orders/{id}/cancel")
    public String cancelOrder(
            @PathVariable int id,
            @RequestParam String email,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String redirectTo,
            RedirectAttributes redirectAttributes) {

        try {
            Order order = orderService.cancelOrderByCustomer(id, email, reason);
            redirectAttributes.addFlashAttribute("success",
                    "Order #" + order.getOrderNumber() + " has been cancelled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        if ("success-page".equals(redirectTo)) {
            return "redirect:/checkout/success/" + id;
        }

        return "redirect:/my-orders?email=" + UriUtils.encode(email.trim(), StandardCharsets.UTF_8);
    }

}
