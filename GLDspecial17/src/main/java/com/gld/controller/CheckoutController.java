package com.gld.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gld.dto.Cart;
import com.gld.dto.CartItem;
import com.gld.dto.CheckoutData;
import com.gld.dto.Order;
import com.gld.dto.PaymentInitData;
import com.gld.repository.OrderRepository;
import com.gld.service.implentation.CartService;
import com.gld.service.implentation.CheckoutService;
import com.gld.service.implentation.CheckoutServiceImpl;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderRepository orderRepository;


    // =========================================================
    // STEP 1: SHOW CHECKOUT FORM (name, phone, address, email...)
    // =========================================================

    @GetMapping("/checkout")
    public String showCheckoutForm(HttpSession session, Model model) {

        Cart cart = cartService.getCart(session);

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        double totalAmount = 0;

        for (CartItem item : cart.getItems()) {
            totalAmount += item.getPrice() * item.getQuantity();
        }

        model.addAttribute("cart", cart);
        model.addAttribute("totalAmount", totalAmount);

        if (!model.containsAttribute("checkoutData")) {
            model.addAttribute("checkoutData", new CheckoutData());
        }

        return "checkout";
    }


    // =========================================================
    // STEP 2: SUBMIT DETAILS -> SEND OTP
    // =========================================================

    @PostMapping("/checkout/send-otp")
    public String sendOtp(
            @ModelAttribute CheckoutData checkoutData,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            checkoutService.initiateCheckout(checkoutData, session);
            return "redirect:/checkout/verify";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("checkoutData", checkoutData);
            return "redirect:/checkout";
        }
    }


    // =========================================================
    // STEP 3: SHOW OTP VERIFICATION PAGE
    // =========================================================

    @GetMapping("/checkout/verify")
    public String showVerifyPage(HttpSession session, Model model) {

        CheckoutData data = (CheckoutData) session.getAttribute(CheckoutServiceImpl.SESSION_KEY);

        if (data == null) {
            return "redirect:/checkout";
        }

        model.addAttribute("maskedEmail", maskEmail(data.getEmail()));

        return "verify-otp";
    }


    // =========================================================
    // STEP 4: SUBMIT OTP -> CREATE RAZORPAY ORDER -> GO TO PAYMENT
    // =========================================================

    @PostMapping("/checkout/verify")
    public String verifyOtp(
            @RequestParam String otp,
            HttpSession session,
            Model model) {

        try {
            checkoutService.verifyOtpAndCreatePayment(otp, session);
            return "redirect:/checkout/payment";

        } catch (Exception e) {

            CheckoutData data = (CheckoutData) session.getAttribute(CheckoutServiceImpl.SESSION_KEY);

            model.addAttribute("error", e.getMessage());
            model.addAttribute("maskedEmail", data != null ? maskEmail(data.getEmail()) : "");

            return "verify-otp";
        }
    }


    // =========================================================
    // STEP 5: SHOW PAYMENT PAGE (RAZORPAY CHECKOUT)
    // =========================================================

    @GetMapping("/checkout/payment")
    public String showPaymentPage(HttpSession session, Model model) {

        CheckoutData data = (CheckoutData) session.getAttribute(CheckoutServiceImpl.SESSION_KEY);
        PaymentInitData paymentData =
                (PaymentInitData) session.getAttribute(CheckoutServiceImpl.PAYMENT_SESSION_KEY);

        if (data == null || !data.isOtpVerified() || paymentData == null) {
            return "redirect:/checkout";
        }

        Cart cart = cartService.getCart(session);

        model.addAttribute("cart", cart);
        model.addAttribute("payment", paymentData);

        return "payment";
    }


    // =========================================================
    // STEP 6: RAZORPAY CALLBACK -> VERIFY SIGNATURE -> PLACE ORDER
    // =========================================================

    @PostMapping("/checkout/payment/verify")
    public String verifyPayment(
            @RequestParam("razorpay_order_id") String razorpayOrderId,
            @RequestParam("razorpay_payment_id") String razorpayPaymentId,
            @RequestParam("razorpay_signature") String razorpaySignature,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Order order = checkoutService.verifyPaymentAndPlaceOrder(
                    razorpayOrderId, razorpayPaymentId, razorpaySignature, session);

            return "redirect:/checkout/success/" + order.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("paymentError", e.getMessage());
            return "redirect:/checkout/payment";
        }
    }


    // =========================================================
    // RESEND OTP
    // =========================================================

    @PostMapping("/checkout/resend-otp")
    public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {

        try {
            checkoutService.resendOtp(session);
            redirectAttributes.addFlashAttribute("message", "A new OTP has been sent to your email.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/checkout/verify";
    }


    // =========================================================
    // ORDER SUCCESS PAGE
    // =========================================================

    @GetMapping("/checkout/success/{id}")
    public String orderSuccess(@PathVariable int id, Model model) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("order", order);

        return "order-success";
    }


    // =========================================================
    // DOWNLOAD / RE-DOWNLOAD INVOICE PDF
    // =========================================================

    @GetMapping("/checkout/invoice/{id}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable int id) throws Exception {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        byte[] pdf = checkoutService.getInvoiceBytes(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
                "attachment",
                "Invoice_" + order.getOrderNumber() + ".pdf"
        );

        return ResponseEntity.ok().headers(headers).body(pdf);
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private String maskEmail(String email) {

        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String name = parts[0];

        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + parts[1];
        }

        return name.substring(0, 2) + "***@" + parts[1];
    }

}
