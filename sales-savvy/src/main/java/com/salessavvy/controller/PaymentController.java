
package com.salessavvy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.salessavvy.dto.PaymentRequest;
import com.salessavvy.dto.PaymentResponse;
import com.salessavvy.dto.RazorpayVerificationRequest;
import com.salessavvy.service.PaymentService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Initiate payment and create Razorpay order
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.initiatePayment(
                authentication.getName(), request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Verify Razorpay payment signature
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyRazorpayPayment(
            Authentication authentication,
            @Valid @RequestBody RazorpayVerificationRequest request) {

        PaymentResponse response = paymentService.verifyRazorpayPayment(
                authentication.getName(), request);

        return ResponseEntity.ok(response);
    }

    // Get payment by ID
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            Authentication authentication,
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(
                        authentication.getName(), paymentId));
    }

    // Get payments for an order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(
            Authentication authentication,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                paymentService.getPaymentsByOrder(
                        authentication.getName(), orderId));
    }

    // Mark an initiated payment as failed
    @PutMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> failPayment(
            Authentication authentication,
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                paymentService.failPayment(
                        authentication.getName(), paymentId));
    }

    // Admin refund
    @PutMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            Authentication authentication,
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                paymentService.refundPayment(
                        authentication.getName(), paymentId));
    }
}
