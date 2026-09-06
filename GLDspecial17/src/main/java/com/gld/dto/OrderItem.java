package com.gld.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Saree.id OR Silk.id at the time of order
    private int productId;

    // SAREE or SILK
    private String productType;

    private String productName;

    // Snapshot of the product's first image at the time of order, so the
    // picture shown in "My Orders" / admin order details never changes even
    // if the product is later edited or removed.
    @jakarta.persistence.Column(length = 1000)
    private String imageUrl;

    private double price;

    private int quantity;

    private double subtotal;

    // ===================== SNAPSHOT OF PRODUCT DETAILS =====================
    // sareeType / silkType
    private String category;

    private String fabric;

    private String color;

    private String design;

    private String weavingType;

    // ===================== SAREE-SPECIFIC SNAPSHOT =====================
    private String occasion;

    private String borderType;

    private Boolean blouseIncluded;

    // ===================== SILK-SPECIFIC SNAPSHOT =====================
    private String origin;

    private String finish;

    private String weight;

    private String width;

    // Product description at the time of order.
    @jakarta.persistence.Column(length = 1000)
    private String description;

    // Kept for orders placed before the structured fields above existed.
    // New orders populate the fields above instead; the UI falls back to
    // this text only when those are empty.
    @jakarta.persistence.Column(length = 1000)
    private String extraInfo;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

}
