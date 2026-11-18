
package com.salessavvy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.salessavvy.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser_Id(Long userId);

    List<Order> findByUser_IdOrderByOrderDateDesc(Long userId);

    // TiDB/MySQL-compatible pessimistic row lock
    @Query(
        value = "SELECT * FROM orders WHERE id = :orderId FOR UPDATE",
        nativeQuery = true
    )
    Optional<Order> findByIdForUpdate(
            @Param("orderId") Long orderId
    );
}
