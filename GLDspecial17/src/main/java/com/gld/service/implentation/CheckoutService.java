package com.gld.service.implentation;

import com.gld.dto.CheckoutData;
import com.gld.dto.Order;
import com.gld.dto.PaymentInitData;

import jakarta.servlet.http.HttpSession;

public interface CheckoutService {

	void initiateCheckout(CheckoutData formData, HttpSession session) throws Exception;

	void resendOtp(HttpSession session) throws Exception;

	// STEP 3: verify the emailed OTP, then create a Razorpay order for the
	// cart total so the customer can be sent to the payment page.
	PaymentInitData verifyOtpAndCreatePayment(String enteredOtp, HttpSession session) throws Exception;

	// STEP 4: verify the Razorpay payment signature and only then place
	// the order in the database.
	Order verifyPaymentAndPlaceOrder(
			String razorpayOrderId,
			String razorpayPaymentId,
			String razorpaySignature,
			HttpSession session) throws Exception;

	byte[] getInvoiceBytes(int orderId) throws Exception;

}
