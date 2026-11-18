
package com.salessavvy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.salessavvy.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // Find cart using customer ID
    Optional<Cart> findByUser_Id(Long userId);

    // Lock the cart row using native MySQL/TiDB SQL
    @Query(
        value = "SELECT * FROM carts WHERE user_id = :userId FOR UPDATE",
        nativeQuery = true
    )
    Optional<Cart> findByUser_IdForUpdate(
            @Param("userId") Long userId
    );
}
