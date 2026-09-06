package com.gld.helper;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

/**
 * Thin wrapper around the Razorpay Java SDK: creating a Razorpay order for
 * the current cart total, and verifying the signature Razorpay sends back
 * once a payment has gone through.
 */
@Component
public class RazorpayHelper {

    @Value("${razorpay.key}")
    private String keyId;

    @Value("${razorpay.secret}")
    private String keySecret;

    public String getKeyId() {
        return keyId;
    }

    /**
     * Creates a Razorpay order for the given rupee amount and returns the
     * raw Razorpay Order object (its "id" field is what the checkout
     * widget on the frontend needs).
     */
    public Order createOrder(double amountInRupees, String receipt) throws RazorpayException {

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        long amountInPaise = Math.round(amountInRupees * 100);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1);

        return client.orders.create(orderRequest);
    }

    /**
     * Verifies the razorpay_order_id / razorpay_payment_id / razorpay_signature
     * triple returned by Razorpay Checkout after a successful payment.
     */
    public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature)
            throws RazorpayException {

        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", razorpayOrderId);
        options.put("razorpay_payment_id", razorpayPaymentId);
        options.put("razorpay_signature", razorpaySignature);

        return Utils.verifyPaymentSignature(options, keySecret);
    }
}
