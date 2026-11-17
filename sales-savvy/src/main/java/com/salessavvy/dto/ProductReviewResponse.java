package com.salessavvy.dto;

import java.time.LocalDateTime;

public class ProductReviewResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String reviewerName;
    private Integer rating;
    private String comment;
    private Boolean approved;
    private LocalDateTime createdAt;

    public ProductReviewResponse() {
    }

    public ProductReviewResponse(
            Long id,
            Long productId,
            String productName,
            String reviewerName,
            Integer rating,
            String comment,
            Boolean approved,
            LocalDateTime createdAt) {

        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.comment = comment;
        this.approved = approved;
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

    public String getReviewerName() {
        return reviewerName;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Boolean getApproved() {
        return approved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}