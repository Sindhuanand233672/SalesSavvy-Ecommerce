
package com.salessavvy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminCustomerDetailsResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        LocalDateTime createdAt,
        Boolean active,
        Integer totalOrders,
        BigDecimal totalSpent,
        List<AdminCustomerOrderResponse> orders
) {
}
