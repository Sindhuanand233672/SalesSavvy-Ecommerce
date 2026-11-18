
package com.salessavvy.dto;

import java.time.LocalDateTime;

import com.salessavvy.entity.OrderStatus;

public class OrderTrackingResponse {

    private Long id;

    private OrderStatus previousStatus;

    private OrderStatus newStatus;

    private String remarks;

    private String updatedBy;

    private LocalDateTime updatedAt;

    public OrderTrackingResponse() {
    }

    // Parameterized constructor
    public OrderTrackingResponse(
            Long id,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String remarks,
            String updatedBy,
            LocalDateTime updatedAt) {

        this.id = id;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.remarks = remarks;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(OrderStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OrderStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
