package com.gld.helper;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Sends transactional email using the Brevo (formerly Sendinblue) HTTPS
 * Transactional Email API instead of raw SMTP.
 *
 * WHY: Railway (and most cloud hosts on free/hobby tiers) block outbound
 * SMTP ports 25/465/587 to prevent spam abuse. That's why OTP/order mails
 * worked on localhost but silently failed/timed out in production. The
 * Brevo API sends mail over plain HTTPS (port 443), so it is unaffected by
 * that restriction and needs no code change per-host.
 *
 * SETUP:
 * 1. Create a free account at https://www.brevo.com
 * 2. Go to Settings -> SMTP & API -> API Keys -> generate a key (starts with "xkeysib-...")
 * 3. Go to Senders -> add and verify the "from" email address you want to send as
 *    (must be verified in Brevo, otherwise sends will be rejected)
 * 4. Set these properties (see application.properties changes below), and on
 *    Railway set them as Environment Variables of the same name instead of
 *    hardcoding them in the file.
 */
@Service
public class MailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String fromEmail;

    @Value("${brevo.sender.name:Vastra Mane}")
    private String fromName;

    private static final String BREVO_ENDPOINT = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    // =========================================================
    // OTP EMAIL
    // =========================================================

    public void sendOtpEmail(String toEmail, String customerName, String otp) throws Exception {

        String html =
                "<div style='font-family:Arial,sans-serif;max-width:480px;margin:auto;border:1px solid #eee2d9;border-radius:12px;overflow:hidden;'>"
              + "<div style='background:#5c1725;color:#fff;padding:20px;text-align:center;'>"
              + "<h2 style='margin:0;font-family:Georgia,serif;'>Vastra Mane</h2>"
              + "<span style='font-size:11px;letter-spacing:2px;'>SILK &amp; SAREES</span>"
              + "</div>"
              + "<div style='padding:25px;color:#29231f;'>"
              + "<p>Hi " + escape(customerName) + ",</p>"
              + "<p>Use the OTP below to verify your email address and confirm your order:</p>"
              + "<div style='text-align:center;margin:25px 0;'>"
              + "<span style='display:inline-block;background:#faf3ee;border:1px dashed #c68b43;"
              + "color:#5c1725;font-size:28px;font-weight:700;letter-spacing:8px;padding:14px 22px;border-radius:10px;'>"
              + otp + "</span></div>"
              + "<p style='font-size:13px;color:#776d67;'>This OTP is valid for 5 minutes. "
              + "If you did not request this, you can safely ignore this email.</p>"
              + "</div></div>";

        sendViaBrevo(toEmail, customerName, "Vastra Mane - Verify your email (OTP)", html, null);
    }

    // =========================================================
    // ORDER CONFIRMATION EMAIL (with PDF bill attached)
    // =========================================================

    public void sendOrderConfirmation(
            String toEmail,
            String customerName,
            String orderNumber,
            double totalAmount,
            byte[] pdfBytes) throws Exception {

        String html =
                "<div style='font-family:Arial,sans-serif;max-width:480px;margin:auto;border:1px solid #eee2d9;border-radius:12px;overflow:hidden;'>"
              + "<div style='background:#5c1725;color:#fff;padding:20px;text-align:center;'>"
              + "<h2 style='margin:0;font-family:Georgia,serif;'>Vastra Mane</h2>"
              + "<span style='font-size:11px;letter-spacing:2px;'>SILK &amp; SAREES</span>"
              + "</div>"
              + "<div style='padding:25px;color:#29231f;'>"
              + "<p>Hi " + escape(customerName) + ",</p>"
              + "<p>Thank you for shopping with us! Your order has been placed successfully.</p>"
              + "<p><strong>Order Number:</strong> " + escape(orderNumber) + "<br>"
              + "<strong>Total Amount:</strong> &#8377;" + totalAmount + "</p>"
              + "<p>Your detailed bill (with saree/silk type and full item details) is attached to this email as a PDF.</p>"
              + "<p style='font-size:13px;color:#776d67;'>We will notify you once your order is shipped.</p>"
              + "</div></div>";

        Attachment attachment = new Attachment("Invoice_" + orderNumber + ".pdf", pdfBytes);

        sendViaBrevo(toEmail, customerName, "Vastra Mane - Order Confirmed! (#" + orderNumber + ")", html, attachment);
    }

    // =========================================================
    // ORDER CANCELLATION EMAIL
    // =========================================================

    public void sendCancellationEmail(
            String toEmail,
            String customerName,
            String orderNumber,
            String reason) throws Exception {

        String html =
                "<div style='font-family:Arial,sans-serif;max-width:480px;margin:auto;border:1px solid #eee2d9;border-radius:12px;overflow:hidden;'>"
              + "<div style='background:#5c1725;color:#fff;padding:20px;text-align:center;'>"
              + "<h2 style='margin:0;font-family:Georgia,serif;'>Vastra Mane</h2>"
              + "<span style='font-size:11px;letter-spacing:2px;'>SILK &amp; SAREES</span>"
              + "</div>"
              + "<div style='padding:25px;color:#29231f;'>"
              + "<p>Hi " + escape(customerName) + ",</p>"
              + "<p>Your order <b>#" + escape(orderNumber) + "</b> has been cancelled as requested.</p>"
              + (reason != null && !reason.isBlank()
                    ? "<p><strong>Reason:</strong> " + escape(reason) + "</p>" : "")
              + "<p style='font-size:13px;color:#776d67;'>If any amount was paid, it will be refunded to your "
              + "original payment method within 5-7 business days. If you did not request this cancellation, "
              + "please contact our support team immediately.</p>"
              + "</div></div>";

        sendViaBrevo(toEmail, customerName, "Vastra Mane - Order Cancelled (#" + orderNumber + ")", html, null);
    }

    // =========================================================
    // ORDER STATUS UPDATE EMAIL (tracking)
    // =========================================================

    public void sendStatusUpdateEmail(
            String toEmail,
            String customerName,
            String orderNumber,
            String status) throws Exception {

        String label;
        String line;
        switch (status) {
            case "CONFIRMED":
                label = "Confirmed";
                line = "Your order has been confirmed and is being prepared.";
                break;
            case "PACKED":
                label = "Packed";
                line = "Great news — your order has been packed and is ready to leave our warehouse.";
                break;
            case "SHIPPED":
                label = "Dispatched";
                line = "Your order has been dispatched and is on its way to you!";
                break;
            case "DELIVERED":
                label = "Delivered";
                line = "Your order has been delivered. We hope you love it!";
                break;
            default:
                label = status;
                line = "Your order status has been updated.";
        }

        String html =
                "<div style='font-family:Arial,sans-serif;max-width:480px;margin:auto;border:1px solid #eee2d9;border-radius:12px;overflow:hidden;'>"
              + "<div style='background:#5c1725;color:#fff;padding:20px;text-align:center;'>"
              + "<h2 style='margin:0;font-family:Georgia,serif;'>Vastra Mane</h2>"
              + "<span style='font-size:11px;letter-spacing:2px;'>SILK &amp; SAREES</span>"
              + "</div>"
              + "<div style='padding:25px;color:#29231f;'>"
              + "<p>Hi " + escape(customerName) + ",</p>"
              + "<p>" + line + "</p>"
              + "<p><strong>Order Number:</strong> " + escape(orderNumber) + "<br>"
              + "<strong>Current Status:</strong> " + escape(label) + "</p>"
              + "<p style='font-size:13px;color:#776d67;'>You can track this order any time from the "
              + "\"My Orders\" page on our website.</p>"
              + "</div></div>";

        sendViaBrevo(toEmail, customerName, "Vastra Mane - Order " + label + " (#" + orderNumber + ")", html, null);
    }

    // =========================================================
    // INTERNAL: Brevo HTTPS API call
    // =========================================================

    private record Attachment(String name, byte[] content) {}

    private void sendViaBrevo(String toEmail, String toName, String subject, String htmlContent, Attachment attachment)
            throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", brevoApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("sender", Map.of("name", fromName, "email", fromEmail));
        body.put("to", List.of(Map.of("email", toEmail, "name", toName == null ? "" : toName)));
        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        if (attachment != null) {
            body.put("attachment", List.of(Map.of(
                    "name", attachment.name(),
                    "content", Base64.getEncoder().encodeToString(attachment.content())
            )));
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(BREVO_ENDPOINT, request, String.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Surface Brevo's error body (e.g. unverified sender, invalid key) instead of a generic 400/401
            throw new Exception("Brevo email send failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("<", "&lt;").replace(">", "&gt;");
    }

}
