
package com.salessavvy.dto;

import java.time.LocalDateTime;

public record AdminCustomerResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        LocalDateTime createdAt,
        Boolean active
) {
}
