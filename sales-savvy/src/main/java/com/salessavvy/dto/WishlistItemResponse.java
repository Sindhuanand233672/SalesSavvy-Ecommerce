//package com.salessavvy.dto;
//
//public class WishlistItemResponse {
//
//    private Long id;
//    private Long productId;
//    private String productName;
//    private String sku;
//    private java.math.BigDecimal price;
//    private String imageUrl;
//
//    public WishlistItemResponse() {
//    }
//
//    public WishlistItemResponse(
//            Long id,
//            Long productId,
//            String productName,
//            String sku,
//            java.math.BigDecimal price,
//            String imageUrl) {
//        this.id = id;
//        this.productId = productId;
//        this.productName = productName;
//        this.sku = sku;
//        this.price = price;
//        this.imageUrl = imageUrl;
//    }
//
//    public Long getId() { return id; }
//    public Long getProductId() { return productId; }
//    public String getProductName() { return productName; }
//    public String getSku() { return sku; }
//    public java.math.BigDecimal getPrice() { return price; }
//    public String getImageUrl() { return imageUrl; }
//}
//



package com.salessavvy.dto;

public class WishlistItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private java.math.BigDecimal price;
    private String imageUrl;
    private Integer stockQuantity;

    public WishlistItemResponse() {
    }

    public WishlistItemResponse(
            Long id,
            Long productId,
            String productName,
            String sku,
            java.math.BigDecimal price,
            String imageUrl,
            Integer stockQuantity) {

        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.price = price;
        this.imageUrl = imageUrl;
        this.stockQuantity = stockQuantity;
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

    public String getSku() {
        return sku;
    }

    public java.math.BigDecimal getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }
}

