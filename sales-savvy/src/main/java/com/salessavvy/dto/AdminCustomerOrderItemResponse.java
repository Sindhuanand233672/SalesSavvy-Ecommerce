
package com.salessavvy.dto;

import java.math.BigDecimal;

public record AdminCustomerOrderItemResponse(
        Long productId,
        String productName,
        String sku,
        String imageUrl,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
