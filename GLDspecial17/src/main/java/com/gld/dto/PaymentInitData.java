package com.gld.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * Not a JPA entity. Temporary holder kept in the HttpSession, describing
 * the Razorpay order that was created for the current cart so the
 * payment page (and its verification step) has everything it needs.
 */
@Data
public class PaymentInitData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String razorpayKeyId;
    private String razorpayOrderId;

    private long amountInPaise;
    private double amountInRupees;
    private String currency;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
}
