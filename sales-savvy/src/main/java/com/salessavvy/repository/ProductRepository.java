
package com.salessavvy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.salessavvy.entity.Product;

public interface ProductRepository
        extends JpaRepository<Product, Long>,
                JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByActiveTrue();
 // Get active products by brand
    List<Product> findByBrandIgnoreCaseAndActiveTrue(String brand);

    // Get active products by category
    List<Product> findByCategory_IdAndActiveTrue(Long categoryId);

    // Search active products by name
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    // Lock product row while modifying stock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdForUpdate(
            @Param("productId") Long productId);
}
