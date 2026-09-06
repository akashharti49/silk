package com.gld.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * Not a JPA entity. This is a temporary holder kept in the HttpSession
 * while the customer's email OTP is being verified. Only after a
 * successful OTP match is an actual Order created in the database.
 */
@Data
public class CheckoutData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;

    private String otp;
    private LocalDateTime otpGeneratedAt;
    private int otpAttempts;

    // true once the OTP has been correctly verified; the payment page
    // is only reachable when this flag is set
    private boolean otpVerified;

}
