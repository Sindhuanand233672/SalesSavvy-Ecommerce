package com.salessavvy.dto;

import java.math.BigDecimal;

public class CouponValidationResponse {

    private String couponCode;
    private boolean valid;
    private String message;
    private BigDecimal orderAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    public CouponValidationResponse() {
    }

    public CouponValidationResponse(String couponCode, boolean valid,
            String message, BigDecimal orderAmount,
            BigDecimal discountAmount, BigDecimal finalAmount) {

        this.couponCode = couponCode;
        this.valid = valid;
        this.message = message;
        this.orderAmount = orderAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }
}