
package com.salessavvy.dto;

import com.salessavvy.entity.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CheckoutRequest {

    @NotNull(message = "Address ID is required")
    @Positive(message = "Address ID must be positive")
    private Long addressId;

    @Size(max = 50, message = "Coupon code cannot exceed 50 characters")
    private String couponCode;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    public CheckoutRequest() {
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
