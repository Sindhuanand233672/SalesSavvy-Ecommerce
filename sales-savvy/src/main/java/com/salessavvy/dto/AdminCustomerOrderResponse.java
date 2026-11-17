
package com.salessavvy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.salessavvy.entity.OrderStatus;

public record AdminCustomerOrderResponse(
        Long orderId,
        LocalDateTime orderDate,
        BigDecimal totalAmount,
        OrderStatus status,
        String shippingAddress,
        String couponCode,
        List<AdminCustomerOrderItemResponse> items
) {
}
