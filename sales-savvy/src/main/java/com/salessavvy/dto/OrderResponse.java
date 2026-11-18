
package com.salessavvy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.salessavvy.entity.OrderStatus;

public class OrderResponse {

    private Long id;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String shippingAddress;
    private String couponCode;
    private List<OrderItemResponse> items;

    // Smart Delivery Information
    private LocalDate estimatedDeliveryDate;
    private LocalTime deliveryStartTime;
    private LocalTime deliveryEndTime;
    private LocalDateTime deliveredAt;
    private String deliveryRemarks;

    public OrderResponse() {
    }

    public OrderResponse(
            Long id,
            LocalDateTime orderDate,
            BigDecimal totalAmount,
            OrderStatus status,
            String shippingAddress,
            String couponCode,
            List<OrderItemResponse> items) {

        this.id = id;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.couponCode = couponCode;
        this.items = items;
    }

    // Existing Getters

    public Long getId() {
        return id;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    // Smart Delivery Getters and Setters

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(
            LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public LocalTime getDeliveryStartTime() {
        return deliveryStartTime;
    }

    public void setDeliveryStartTime(
            LocalTime deliveryStartTime) {
        this.deliveryStartTime = deliveryStartTime;
    }

    public LocalTime getDeliveryEndTime() {
        return deliveryEndTime;
    }

    public void setDeliveryEndTime(
            LocalTime deliveryEndTime) {
        this.deliveryEndTime = deliveryEndTime;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(
            LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getDeliveryRemarks() {
        return deliveryRemarks;
    }

    public void setDeliveryRemarks(
            String deliveryRemarks) {
        this.deliveryRemarks = deliveryRemarks;
    }
}
