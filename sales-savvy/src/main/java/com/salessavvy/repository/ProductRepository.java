
package com.salessavvy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.salessavvy.entity.Product;

public interface ProductRepository
        extends JpaRepository<Product, Long>,
                JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByActiveTrue();

    List<Product> findByBrandIgnoreCaseAndActiveTrue(String brand);

    List<Product> findByCategory_IdAndActiveTrue(Long categoryId);

    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    // Lock product row while modifying stock (TiDB/MySQL compatible)
    @Query(
        value = "SELECT * FROM products WHERE id = :productId FOR UPDATE",
        nativeQuery = true
    )
    Optional<Product> findByIdForUpdate(
            @Param("productId") Long productId
    );
}
