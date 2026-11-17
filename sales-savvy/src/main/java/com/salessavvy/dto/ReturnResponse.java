package com.salessavvy.dto;

import java.time.LocalDateTime;

import com.salessavvy.entity.ReturnStatus;

public class ReturnResponse {

    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Long productId;
    private String productName;
    private String customerEmail;
    private Integer quantity;
    private String reason;
    private ReturnStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime completedAt;

    public ReturnResponse(
            Long id,
            Long orderId,
            Long orderItemId,
            Long productId,
            String productName,
            String customerEmail,
            Integer quantity,
            String reason,
            ReturnStatus status,
            String adminNote,
            LocalDateTime createdAt,
            LocalDateTime reviewedAt,
            LocalDateTime completedAt) {

        this.id = id;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productName = productName;
        this.customerEmail = customerEmail;
        this.quantity = quantity;
        this.reason = reason;
        this.status = status;
        this.adminNote = adminNote;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getReason() {
        return reason;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}