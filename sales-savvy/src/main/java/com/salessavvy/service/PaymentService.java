
package com.salessavvy.service;

import java.util.List;

import com.salessavvy.dto.PaymentRequest;
import com.salessavvy.dto.PaymentResponse;
import com.salessavvy.dto.RazorpayVerificationRequest;

public interface PaymentService {

    PaymentResponse initiatePayment(String email, PaymentRequest request);

    PaymentResponse getPaymentById(String email, Long paymentId);

    List<PaymentResponse> getPaymentsByOrder(String email, Long orderId);

    PaymentResponse verifyRazorpayPayment(
            String email,
            RazorpayVerificationRequest request);

    PaymentResponse failPayment(String email, Long paymentId);

    PaymentResponse refundPayment(String email, Long paymentId);

    PaymentResponse collectCodPayment(String email, Long orderId);
}
