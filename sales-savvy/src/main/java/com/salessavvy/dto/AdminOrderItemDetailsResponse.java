package com.salessavvy.dto;

import java.math.BigDecimal;

public class AdminOrderItemDetailsResponse {

    private Long itemId;
    private Long productId;
    private String productName;
    private String sku;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public AdminOrderItemDetailsResponse(
            Long itemId,
            Long productId,
            String productName,
            String sku,
            String imageUrl,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal) {

        this.itemId = itemId;
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public Long getItemId() { return itemId; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getSku() { return sku; }
    public String getImageUrl() { return imageUrl; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
}