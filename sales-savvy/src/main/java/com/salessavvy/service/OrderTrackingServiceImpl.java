
package com.salessavvy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salessavvy.dto.OrderTrackingResponse;
import com.salessavvy.entity.Order;
import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.OrderTracking;
import com.salessavvy.repository.OrderTrackingRepository;

@Service
@Transactional
public class OrderTrackingServiceImpl
        implements OrderTrackingService {

    private final OrderTrackingRepository trackingRepository;

    // Constructor Injection
    public OrderTrackingServiceImpl(
            OrderTrackingRepository trackingRepository) {

        this.trackingRepository = trackingRepository;
    }

    // Record every order status change
    @Override
    public void recordStatusChange(
            Order order,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String updatedBy,
            String remarks) {

        OrderTracking tracking = new OrderTracking();

        tracking.setOrder(order);
        tracking.setPreviousStatus(previousStatus);
        tracking.setNewStatus(newStatus);
        tracking.setUpdatedBy(updatedBy);
        tracking.setRemarks(remarks);

        trackingRepository.save(tracking);
    }

    // Retrieve complete order tracking history
    @Override
    @Transactional(readOnly = true)
    public List<OrderTrackingResponse> getTrackingHistory(
            Long orderId) {

        return trackingRepository
                .findByOrder_IdOrderByUpdatedAtAsc(orderId)
                .stream()
                .map(tracking -> new OrderTrackingResponse(
                        tracking.getId(),
                        tracking.getPreviousStatus(),
                        tracking.getNewStatus(),
                        tracking.getRemarks(),
                        tracking.getUpdatedBy(),
                        tracking.getUpdatedAt()
                ))
                .toList();
    }
}
