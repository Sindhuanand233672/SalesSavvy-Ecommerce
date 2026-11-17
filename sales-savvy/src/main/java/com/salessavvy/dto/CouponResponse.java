package com.salessavvy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.salessavvy.entity.DiscountType;

public class CouponResponse {

    private Long id;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumDiscountAmount;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private Integer usageLimit;
    private Integer usageCount;
    private Boolean active;
    private LocalDateTime createdAt;

    public CouponResponse() {
    }

    public CouponResponse(Long id, String code, String description,
            DiscountType discountType, BigDecimal discountValue,
            BigDecimal minimumOrderAmount, BigDecimal maximumDiscountAmount,
            LocalDateTime startDate, LocalDateTime expiryDate,
            Integer usageLimit, Integer usageCount, Boolean active,
            LocalDateTime createdAt) {

        this.id = id;
        this.code = code;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minimumOrderAmount = minimumOrderAmount;
        this.maximumDiscountAmount = maximumDiscountAmount;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.usageLimit = usageLimit;
        this.usageCount = usageCount;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public BigDecimal getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public BigDecimal getMaximumDiscountAmount() {
        return maximumDiscountAmount;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}