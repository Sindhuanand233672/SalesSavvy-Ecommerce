
package com.salessavvy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.salessavvy.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    List<Payment> findByOrder_Id(Long orderId);

    List<Payment> findByOrder_IdOrderByCreatedAtDesc(Long orderId);

    // Lock individual payment row using native TiDB/MySQL SQL
    @Query(
        value = "SELECT * FROM payments WHERE id = :paymentId FOR UPDATE",
        nativeQuery = true
    )
    Optional<Payment> findByIdForUpdate(
            @Param("paymentId") Long paymentId
    );

    // Lock all payments belonging to an order
    @Query(
        value = "SELECT * FROM payments WHERE order_id = :orderId FOR UPDATE",
        nativeQuery = true
    )
    List<Payment> findByOrderIdForUpdate(
            @Param("orderId") Long orderId
    );
}
