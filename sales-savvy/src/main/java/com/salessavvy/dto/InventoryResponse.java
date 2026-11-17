
package com.salessavvy.dto;

public class InventoryResponse {

    private Long productId;
    private String productName;
    private String sku;
    private Integer stockQuantity;
    private String message;

    public InventoryResponse() {
    }

    public InventoryResponse(
            Long productId,
            String productName,
            String sku,
            Integer stockQuantity,
            String message) {

        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.stockQuantity = stockQuantity;
        this.message = message;
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

    public String getMessage() {
        return message;
    }
}
