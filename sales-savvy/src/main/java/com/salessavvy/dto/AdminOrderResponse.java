
package com.salessavvy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.PaymentMethod;
import com.salessavvy.entity.PaymentStatus;

public class AdminOrderResponse {

    private Long orderId;
    private String customerName;
    private String customerEmail;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime orderDate;

    public AdminOrderResponse(
            Long orderId,
            String customerName,
            String customerEmail,
            BigDecimal totalAmount,
            OrderStatus status,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            LocalDateTime orderDate) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.orderDate = orderDate;
    }

    public Long getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getOrderDate() { return orderDate; }
}
