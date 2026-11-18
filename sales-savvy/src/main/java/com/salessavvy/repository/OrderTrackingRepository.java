
package com.salessavvy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salessavvy.entity.OrderTracking;

public interface OrderTrackingRepository
        extends JpaRepository<OrderTracking, Long> {

    List<OrderTracking> findByOrder_IdOrderByUpdatedAtAsc(
            Long orderId);
}
