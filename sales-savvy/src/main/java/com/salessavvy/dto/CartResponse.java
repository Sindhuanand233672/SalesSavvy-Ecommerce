
package com.salessavvy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CartResponse {

    private Long id;
    private LocalDateTime createdAt;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal totalAmount;

    public CartResponse() {
    }

    public CartResponse(
            Long id,
            LocalDateTime createdAt,
            List<CartItemResponse> items,
            Integer totalItems,
            BigDecimal totalAmount) {

        this.id = id;
        this.createdAt = createdAt;
        this.items = items;
        this.totalItems = totalItems;
        this.totalAmount = totalAmount;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
