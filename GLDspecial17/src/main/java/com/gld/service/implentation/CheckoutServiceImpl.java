package com.gld.service.implentation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gld.dto.Cart;
import com.gld.dto.CartItem;
import com.gld.dto.CheckoutData;
import com.gld.dto.Order;
import com.gld.dto.OrderItem;
import com.gld.dto.PaymentInitData;
import com.gld.dto.Saree;
import com.gld.dto.Silk;
import com.gld.helper.MailService;
import com.gld.helper.PdfInvoiceService;
import com.gld.helper.RazorpayHelper;
import com.gld.repository.CartItemRepository;
import com.gld.repository.CartRepository;
import com.gld.repository.OrderRepository;
import com.gld.repository.SareeRepository;
import com.gld.repository.SilkRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private static final long OTP_VALID_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 5;

    public static final String SESSION_KEY = "CHECKOUT_DATA";
    public static final String PAYMENT_SESSION_KEY = "PAYMENT_INIT_DATA";

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private SareeRepository sareeRepository;

    @Autowired
    private SilkRepository silkRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private PdfInvoiceService pdfInvoiceService;

    @Autowired
    private RazorpayHelper razorpayHelper;


    // =========================================================
    // STEP 1: VALIDATE DETAILS + SEND OTP
    // =========================================================

    @Override
    public void initiateCheckout(CheckoutData formData, HttpSession session) throws Exception {

        Cart cart = cartService.getCart(session);

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Your cart is empty.");
        }

        validateDetails(formData);

        String otp = generateOtp();

        formData.setOtp(otp);
        formData.setOtpGeneratedAt(LocalDateTime.now());
        formData.setOtpAttempts(0);
        formData.setOtpVerified(false);

        session.setAttribute(SESSION_KEY, formData);
        session.removeAttribute(PAYMENT_SESSION_KEY);

        try {
            mailService.sendOtpEmail(formData.getEmail(), formData.getName(), otp);
        } catch (Exception e) {
            session.removeAttribute(SESSION_KEY);
            throw new IllegalStateException(
                    "Could not send the verification email. Please check the email address and try again.");
        }
    }


    // =========================================================
    // RESEND OTP
    // =========================================================

    @Override
    public void resendOtp(HttpSession session) throws Exception {

        CheckoutData data = (CheckoutData) session.getAttribute(SESSION_KEY);

        if (data == null) {
            throw new IllegalStateException("Your checkout session has expired. Please start checkout again.");
        }

        String otp = generateOtp();

        data.setOtp(otp);
        data.setOtpGeneratedAt(LocalDateTime.now());
        data.setOtpAttempts(0);

        try {
            mailService.sendOtpEmail(data.getEmail(), data.getName(), otp);
        } catch (Exception e) {
            throw new IllegalStateException("Could not resend the OTP. Please try again.");
        }
    }


    // =========================================================
    // STEP 2: VERIFY OTP -> CREATE RAZORPAY ORDER FOR PAYMENT
    // =========================================================

    @Override
    public PaymentInitData verifyOtpAndCreatePayment(String enteredOtp, HttpSession session) throws Exception {

        CheckoutData data = (CheckoutData) session.getAttribute(SESSION_KEY);

        if (data == null) {
            throw new IllegalStateException("Your checkout session has expired. Please start checkout again.");
        }

        if (Duration.between(data.getOtpGeneratedAt(), LocalDateTime.now()).toMinutes() >= OTP_VALID_MINUTES) {
            session.removeAttribute(SESSION_KEY);
            throw new IllegalStateException("Your OTP has expired. Please start checkout again.");
        }

        data.setOtpAttempts(data.getOtpAttempts() + 1);

        if (data.getOtpAttempts() > MAX_OTP_ATTEMPTS) {
            session.removeAttribute(SESSION_KEY);
            throw new IllegalStateException("Too many incorrect attempts. Please start checkout again.");
        }

        if (enteredOtp == null || !data.getOtp().equals(enteredOtp.trim())) {
            int left = MAX_OTP_ATTEMPTS - data.getOtpAttempts();
            throw new IllegalStateException(
                    "Incorrect OTP. Please try again. (" + Math.max(left, 0) + " attempts left)");
        }

        Cart cart = cartService.getCart(session);

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            session.removeAttribute(SESSION_KEY);
            throw new IllegalStateException("Your cart is empty.");
        }

        double total = 0;
        for (CartItem item : cart.getItems()) {
            total += item.getPrice() * item.getQuantity();
        }

        data.setOtpVerified(true);

        // ============== CREATE RAZORPAY ORDER FOR THE CART TOTAL ==============

        com.razorpay.Order rpOrder = razorpayHelper.createOrder(total, "rcpt_" + System.currentTimeMillis());

        PaymentInitData paymentData = new PaymentInitData();
        paymentData.setRazorpayKeyId(razorpayHelper.getKeyId());
        paymentData.setRazorpayOrderId(rpOrder.get("id"));
        paymentData.setAmountInPaise(Math.round(total * 100));
        paymentData.setAmountInRupees(total);
        paymentData.setCurrency("INR");
        paymentData.setCustomerName(data.getName());
        paymentData.setCustomerEmail(data.getEmail());
        paymentData.setCustomerPhone(data.getPhone());

        session.setAttribute(PAYMENT_SESSION_KEY, paymentData);

        return paymentData;
    }


    // =========================================================
    // STEP 3: VERIFY PAYMENT SIGNATURE + PLACE ORDER + EMAIL PDF BILL
    // =========================================================

    @Override
    @Transactional
    public Order verifyPaymentAndPlaceOrder(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature,
            HttpSession session) throws Exception {

        CheckoutData data = (CheckoutData) session.getAttribute(SESSION_KEY);
        PaymentInitData paymentData = (PaymentInitData) session.getAttribute(PAYMENT_SESSION_KEY);

        if (data == null || !data.isOtpVerified() || paymentData == null) {
            throw new IllegalStateException("Your checkout session has expired. Please start checkout again.");
        }

        if (razorpayOrderId == null || !razorpayOrderId.equals(paymentData.getRazorpayOrderId())) {
            throw new IllegalStateException("This payment does not match your current order. Please try again.");
        }

        boolean valid;
        try {
            valid = razorpayHelper.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);
        } catch (Exception e) {
            valid = false;
        }

        if (!valid) {
            throw new IllegalStateException("We could not verify your payment. Please try paying again.");
        }

        Cart cart = cartService.getCart(session);

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            session.removeAttribute(SESSION_KEY);
            session.removeAttribute(PAYMENT_SESSION_KEY);
            throw new IllegalStateException("Your cart is empty.");
        }

        // ============== BUILD ORDER ==============

        Order order = new Order();

        order.setOrderNumber(generateOrderNumber());
        order.setCustomerName(data.getName());
        order.setPhone(data.getPhone());
        order.setEmail(data.getEmail());
        order.setAddress(data.getAddress());
        order.setCity(data.getCity());
        order.setState(data.getState());
        order.setPincode(data.getPincode());
        order.setEmailVerified(true);
        order.setOrderDate(LocalDateTime.now());

        order.setRazorpayOrderId(razorpayOrderId);
        order.setRazorpayPaymentId(razorpayPaymentId);
        order.setPaymentMethod("Razorpay");
        order.setPaymentStatus("PAID");

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (CartItem ci : cart.getItems()) {

            OrderItem oi = new OrderItem();

            oi.setProductId(ci.getProductId());
            oi.setProductType(ci.getProductType());
            oi.setProductName(ci.getProductName());
            oi.setPrice(ci.getPrice());
            oi.setQuantity(ci.getQuantity());
            oi.setSubtotal(ci.getPrice() * ci.getQuantity());
            oi.setOrder(order);

            fillProductSnapshot(oi, ci);

            orderItems.add(oi);
            total += oi.getSubtotal();
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);

        order = orderRepository.save(order);

        // ============== GENERATE PDF BILL ==============

        byte[] pdf = pdfInvoiceService.generateInvoice(order);

        // ============== EMAIL THE BILL ==============

        try {
            mailService.sendOrderConfirmation(
                    order.getEmail(),
                    order.getCustomerName(),
                    order.getOrderNumber(),
                    order.getTotalAmount(),
                    pdf
            );
        } catch (Exception e) {
            // Order is already placed successfully; do not fail the whole
            // checkout just because the confirmation email could not be sent.
        }

        // ============== CLEAR THE CART ==============

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);

        session.removeAttribute(SESSION_KEY);
        session.removeAttribute(PAYMENT_SESSION_KEY);

        return order;
    }


    // =========================================================
    // DOWNLOAD INVOICE AGAIN
    // =========================================================

    @Override
    public byte[] getInvoiceBytes(int orderId) throws Exception {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found."));

        return pdfInvoiceService.generateInvoice(order);
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private void fillProductSnapshot(OrderItem oi, CartItem ci) {

        if ("SAREE".equals(ci.getProductType())) {

            sareeRepository.findById(ci.getProductId()).ifPresent((Saree s) -> {

                oi.setCategory(s.getSareeType());
                oi.setFabric(s.getFabric());
                oi.setColor(s.getColor());
                oi.setDesign(s.getDesign());
                oi.setWeavingType(s.getWeavingType());
                oi.setDescription(s.getDescription());
                oi.setOccasion(s.getOccasion());
                oi.setBorderType(s.getBorderType());
                oi.setBlouseIncluded(s.isBlouseIncluded());

                if (s.getImageLinks() != null && !s.getImageLinks().isEmpty()) {
                    oi.setImageUrl(s.getImageLinks().get(0));
                }

                StringBuilder extra = new StringBuilder();

                if (s.getOccasion() != null) {
                    extra.append("Occasion: ").append(s.getOccasion()).append("  ");
                }
                if (s.getBorderType() != null && !s.getBorderType().isEmpty()) {
                    extra.append("Border: ").append(s.getBorderType()).append("  ");
                }
                extra.append("Blouse Included: ").append(s.isBlouseIncluded() ? "Yes" : "No");

                oi.setExtraInfo(extra.toString());
            });

        } else {

            silkRepository.findById(ci.getProductId()).ifPresent((Silk s) -> {

                oi.setCategory(s.getSilkType());
                oi.setFabric(s.getFabric());
                oi.setColor(s.getColor());
                oi.setDesign(s.getDesign());
                oi.setWeavingType(s.getWeavingType());
                oi.setDescription(s.getDescription());
                oi.setOrigin(s.getOrigin());
                oi.setFinish(s.getFinish());
                oi.setWeight(s.getWeight());
                oi.setWidth(s.getWidth());

                if (s.getImageLinks() != null && !s.getImageLinks().isEmpty()) {
                    oi.setImageUrl(s.getImageLinks().get(0));
                }

                StringBuilder extra = new StringBuilder();

                if (s.getOrigin() != null) {
                    extra.append("Origin: ").append(s.getOrigin()).append("  ");
                }
                if (s.getFinish() != null && !s.getFinish().isEmpty()) {
                    extra.append("Finish: ").append(s.getFinish()).append("  ");
                }
                if (s.getWeight() != null && !s.getWeight().isEmpty()) {
                    extra.append("Weight: ").append(s.getWeight()).append("  ");
                }
                if (s.getWidth() != null && !s.getWidth().isEmpty()) {
                    extra.append("Width: ").append(s.getWidth());
                }

                oi.setExtraInfo(extra.toString());
            });
        }
    }

    private void validateDetails(CheckoutData data) {

        if (isBlank(data.getName()) || data.getName().trim().length() < 3) {
            throw new IllegalStateException("Please enter your full name (at least 3 characters).");
        }
        if (isBlank(data.getPhone()) || !data.getPhone().trim().matches("^[6-9][0-9]{9}$")) {
            throw new IllegalStateException("Please enter a valid 10-digit mobile number.");
        }
        if (isBlank(data.getEmail()) || !data.getEmail().trim().matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalStateException("Please enter a valid email address.");
        }
        if (isBlank(data.getAddress()) || data.getAddress().trim().length() < 10) {
            throw new IllegalStateException("Please enter your complete delivery address.");
        }
        if (isBlank(data.getCity())) {
            throw new IllegalStateException("Please enter your city.");
        }
        if (isBlank(data.getState())) {
            throw new IllegalStateException("Please enter your state.");
        }
        if (isBlank(data.getPincode()) || !data.getPincode().trim().matches("^[0-9]{6}$")) {
            throw new IllegalStateException("Please enter a valid 6-digit pincode.");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private String generateOrderNumber() {
        return "VM" + System.currentTimeMillis();
    }

}
