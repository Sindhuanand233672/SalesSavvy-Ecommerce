
package com.salessavvy.service;

import java.util.List;

import com.salessavvy.dto.OrderTrackingResponse;
import com.salessavvy.entity.Order;
import com.salessavvy.entity.OrderStatus;

public interface OrderTrackingService {

    void recordStatusChange(
            Order order,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String updatedBy,
            String remarks);

    List<OrderTrackingResponse> getTrackingHistory(
            Long orderId);
}
