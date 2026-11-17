package com.salessavvy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.PaymentMethod;
import com.salessavvy.entity.PaymentStatus;

public class AdminOrderDetailsResponse {

    private Long orderId;
    private String customerName;
    private String customerEmail;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private String shippingAddress;
    private String couponCode;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private LocalDateTime paymentDate;

    private List<AdminOrderItemDetailsResponse> items;

    public AdminOrderDetailsResponse(
            Long orderId,
            String customerName,
            String customerEmail,
            BigDecimal totalAmount,
            OrderStatus status,
            LocalDateTime orderDate,
            String shippingAddress,
            String couponCode,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            String transactionId,
            LocalDateTime paymentDate,
            List<AdminOrderItemDetailsResponse> items) {

        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
        this.shippingAddress = shippingAddress;
        this.couponCode = couponCode;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionId = transactionId;
        this.paymentDate = paymentDate;
        this.items = items;
    }

    public Long getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public String getShippingAddress() { return shippingAddress; }
    public String getCouponCode() { return couponCode; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public String getTransactionId() { return transactionId; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public List<AdminOrderItemDetailsResponse> getItems() { return items; }
}