
package com.salessavvy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.ProductReview;

public interface ProductReviewRepository
        extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProduct_IdAndApprovedTrueOrderByCreatedAtDesc(
            Long productId);

    List<ProductReview> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<ProductReview> findByUser_IdAndProduct_Id(
            Long userId, Long productId);

    List<ProductReview> findByApprovedFalseOrderByCreatedAtDesc();

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    @Query("""
        SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END
        FROM OrderItem oi
        WHERE oi.order.user.id = :userId
          AND oi.order.status = :status
          AND oi.product.id = :productId
        """)
    boolean hasDeliveredProduct(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("status") OrderStatus status);
}
