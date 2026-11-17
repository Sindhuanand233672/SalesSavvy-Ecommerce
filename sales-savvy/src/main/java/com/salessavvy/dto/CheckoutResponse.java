
package com.salessavvy.dto;

import java.math.BigDecimal;

public class CheckoutResponse {

    private String message;

    private Long orderId;

    private BigDecimal subtotal;

    private String couponCode;

    private BigDecimal discountAmount;

    private BigDecimal finalAmount;

    private OrderResponse order;

    private PaymentResponse payment;

    public CheckoutResponse() {
    }

    public CheckoutResponse(
            String message,
            Long orderId,
            BigDecimal subtotal,
            String couponCode,
            BigDecimal discountAmount,
            BigDecimal finalAmount,
            OrderResponse order,
            PaymentResponse payment) {

        this.message = message;
        this.orderId = orderId;
        this.subtotal = subtotal;
        this.couponCode = couponCode;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.order = order;
        this.payment = payment;
    }

    public String getMessage() {
        return message;
    }

    public Long getOrderId() {
        return orderId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public OrderResponse getOrder() {
        return order;
    }

    public PaymentResponse getPayment() {
        return payment;
    }
}
