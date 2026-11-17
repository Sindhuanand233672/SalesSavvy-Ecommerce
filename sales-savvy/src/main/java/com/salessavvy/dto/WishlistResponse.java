package com.salessavvy.dto;

import java.time.LocalDateTime;
import java.util.List;

public class WishlistResponse {

    private Long id;
    private LocalDateTime createdAt;
    private List<WishlistItemResponse> items;

    public WishlistResponse() {
    }

    public WishlistResponse(
            Long id,
            LocalDateTime createdAt,
            List<WishlistItemResponse> items) {
        this.id = id;
        this.createdAt = createdAt;
        this.items = items;
    }

    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<WishlistItemResponse> getItems() { return items; }
}