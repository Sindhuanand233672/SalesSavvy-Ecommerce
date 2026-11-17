
package com.salessavvy.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.salessavvy.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Modifying(flushAutomatically = true)
    @Query("""
        UPDATE Coupon c
        SET c.usageCount = c.usageCount + 1
        WHERE UPPER(c.code) = UPPER(:code)
        AND c.active = true
        AND c.usageCount < c.usageLimit
        AND c.startDate <= :now
        AND c.expiryDate >= :now
    """)
    int incrementUsageIfAvailable(
            @Param("code") String code,
            @Param("now") LocalDateTime now
    );
}
