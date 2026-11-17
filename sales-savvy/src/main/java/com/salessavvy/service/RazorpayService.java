
package com.salessavvy.service;
import com.razorpay.Refund;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.razorpay.Payment;
import com.razorpay.RazorpayException;
@Service
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final String keyId;
    private final String keySecret;

    public RazorpayService(
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret)
            throws RazorpayException {

        this.keyId = keyId;
        this.keySecret = keySecret;
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
    }
    public Refund createFullRefund(String paymentId)
            throws RazorpayException {

        return razorpayClient.payments.refund(paymentId);
    }

    public Order createOrder(BigDecimal amount, String receipt)
            throws RazorpayException {

        long amountInPaise = amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();

        JSONObject request = new JSONObject();
        request.put("amount", amountInPaise);
        request.put("currency", "INR");
        request.put("receipt", receipt);

        return razorpayClient.orders.create(request);
    }
    public String getVerifiedPaymentStatus(
            String paymentId,
            String expectedOrderId,
            BigDecimal expectedAmount)
            throws RazorpayException {

        Payment payment = razorpayClient.payments.fetch(paymentId);

        String orderId = payment.get("order_id").toString();
        String currency = payment.get("currency").toString();
        String status = payment.get("status").toString();

        long actualAmount = Long.parseLong(
                payment.get("amount").toString());

        long expectedAmountInPaise = expectedAmount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();

        if (!expectedOrderId.equals(orderId)
                || !"INR".equalsIgnoreCase(currency)
                || actualAmount != expectedAmountInPaise) {
            throw new IllegalStateException(
                    "Razorpay payment details do not match");
        }

        return status;
    }

    public boolean verifyPaymentSignature(
            String orderId,
            String paymentId,
            String signature) {

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(options, keySecret);

        } catch (RazorpayException | RuntimeException ex) {
            return false;
        }
    }

    public boolean verifyCapturedPayment(
            String paymentId,
            String expectedOrderId,
            BigDecimal expectedAmount) {

        try {
            Payment razorpayPayment =
                    razorpayClient.payments.fetch(paymentId);

            String actualOrderId =
                    razorpayPayment.get("order_id").toString();

            String status =
                    razorpayPayment.get("status").toString();

            long actualAmount =
                    Long.parseLong(
                            razorpayPayment.get("amount").toString());

            String currency =
                    razorpayPayment.get("currency").toString();

            long expectedAmountInPaise = expectedAmount
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .longValueExact();

            return expectedOrderId.equals(actualOrderId)
                    && "captured".equalsIgnoreCase(status)
                    && "INR".equalsIgnoreCase(currency)
                    && actualAmount == expectedAmountInPaise;

        } catch (RazorpayException | RuntimeException ex) {
            return false;
        }
    }

    public String getKeyId() {
        return keyId;
    }
}
