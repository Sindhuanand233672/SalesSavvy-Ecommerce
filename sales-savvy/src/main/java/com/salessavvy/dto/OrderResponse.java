
package com.salessavvy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
}
