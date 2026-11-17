
package com.salessavvy.dto;

import java.time.LocalDateTime;

import com.salessavvy.entity.InventoryMovementType;

public class InventoryMovementResponse {

    private Long id;
    private Long productId;
    private String productName;
    private InventoryMovementType movementType;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private String reason;
    private String performedBy;
    private LocalDateTime createdAt;

    public InventoryMovementResponse(
            Long id,
            Long productId,
            String productName,
            InventoryMovementType movementType,
            Integer quantity,
            Integer previousStock,
            Integer newStock,
            String reason,
            String performedBy,
            LocalDateTime createdAt) {

        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.movementType = movementType;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.reason = reason;
        this.performedBy = performedBy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public InventoryMovementType getMovementType() {
        return movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getPreviousStock() {
        return previousStock;
    }

    public Integer getNewStock() {
        return newStock;
    }

    public String getReason() {
        return reason;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
