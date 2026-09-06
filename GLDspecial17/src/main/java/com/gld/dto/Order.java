package com.gld.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Human friendly order number shown to the customer
    private String orderNumber;

    // ===================== CUSTOMER DETAILS =====================
    private String customerName;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;

    private boolean emailVerified;

    private double totalAmount;

    private LocalDateTime orderDate;

    // ===================== PAYMENT DETAILS (RAZORPAY) =====================
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String paymentMethod;

    // PENDING, PAID, FAILED
    private String paymentStatus = "PENDING";

    // ===================== ORDER STATUS =====================
    // PLACED, CONFIRMED, PACKED, SHIPPED, DELIVERED, CANCELLED
    private String status = "PLACED";

    @jakarta.persistence.Column(length = 500)
    private String cancelReason;

    // Who cancelled it: CUSTOMER or ADMIN
    private String cancelledBy;

    private LocalDateTime cancelledAt;

    // ===================== ORDER TRACKING TIMELINE =====================
    // Stamped the first time the order reaches each stage, used to draw the
    // "track my order" progress timeline for both the customer and the admin.
    private LocalDateTime confirmedAt;
    private LocalDateTime packedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    // =========================================================
    // TRACKING HELPERS (not persisted — derived from status)
    // =========================================================

    /**
     * Numeric step of the order in the PLACED -> CONFIRMED -> PACKED ->
     * SHIPPED -> DELIVERED journey. Returns 0 for CANCELLED/unknown so the
     * visual tracker can be hidden for those orders.
     */
    @jakarta.persistence.Transient
    public int getProgressStep() {
        if (status == null) return 0;
        switch (status) {
            case "PLACED": return 1;
            case "CONFIRMED": return 2;
            case "PACKED": return 3;
            case "SHIPPED": return 4;
            case "DELIVERED": return 5;
            default: return 0; // CANCELLED
        }
    }

    /** Friendly label for the status badge (keeps the DB value unchanged). */
    @jakarta.persistence.Transient
    public String getStatusLabel() {
        if ("SHIPPED".equals(status)) return "Dispatched";
        return status;
    }

}
