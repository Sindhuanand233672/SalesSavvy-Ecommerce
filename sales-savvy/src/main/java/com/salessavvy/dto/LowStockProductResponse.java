
package com.salessavvy.dto;

public class LowStockProductResponse {

    private Long productId;
    private String productName;
    private String sku;
    private Integer stockQuantity;

    public LowStockProductResponse(
            Long productId,
            String productName,
            String sku,
            Integer stockQuantity) {

        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.stockQuantity = stockQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getSku() {
        return sku;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }
}
