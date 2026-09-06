package com.gld.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gld.dto.Cart;
import com.gld.dto.CartItem;
import com.gld.service.implentation.CartService;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @PostMapping("/cart/add/saree/{id}")
    public String addSareeToCart(
            @PathVariable int id,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String message = cartService.addSareeToCart(id, session);

        redirectAttributes.addFlashAttribute("message", message);

        String referer = request.getHeader("Referer");

        if (referer != null && !referer.isEmpty()) {
            String separator = referer.contains("?") ? "&" : "?";
            return "redirect:" + referer + separator + "scrollTo=" + id;
        }

        return "redirect:/silks";
    }

    @PostMapping("/cart/add/silk/{id}")
    public String addSilkToCart(
            @PathVariable int id,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String message = cartService.addSilkToCart(id, session);

        redirectAttributes.addFlashAttribute("message", message);

        String referer = request.getHeader("Referer");

        if (referer != null && !referer.isEmpty()) {
            String separator = referer.contains("?") ? "&" : "?";
            return "redirect:" + referer + separator + "scrollTo=" + id;
        }

        return "redirect:/silks";
    }

    @PostMapping("/cart/remove/silk/{id}")
    public String removeSilk(
            @PathVariable int id,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        String message = cartService.removeSilkFromCart(id, session);

        redirectAttributes.addFlashAttribute("message", message);

        String referer = request.getHeader("Referer");

        if (referer != null && !referer.isEmpty()) {
            String separator = referer.contains("?") ? "&" : "?";
            return "redirect:" + referer + separator + "scrollTo=" + id;
        }

        return "redirect:/silks";
    }

    @PostMapping("/cart/remove/saree/{id}")
    public String removeSaree(
            @PathVariable int id,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        String message = cartService.removeSareeFromCart(id, session);

        redirectAttributes.addFlashAttribute("message", message);

        String referer = request.getHeader("Referer");

        if (referer != null && !referer.isEmpty()) {
            String separator = referer.contains("?") ? "&" : "?";
            return "redirect:" + referer + separator + "scrollTo=" + id;
        }

        return "redirect:/silks";
    }
    
    @GetMapping("/cart")
    public String viewCart(
            HttpSession session,
            Model model) {

        Cart cart = cartService.getCart(session);

        model.addAttribute("cart", cart);

        double totalAmount = 0;

        if (cart != null && cart.getItems() != null) {

            for (CartItem item : cart.getItems()) {

                totalAmount +=
                        item.getPrice() * item.getQuantity();
            }
        }

        model.addAttribute("totalAmount", totalAmount);

        return "cart";
    }
    
   }