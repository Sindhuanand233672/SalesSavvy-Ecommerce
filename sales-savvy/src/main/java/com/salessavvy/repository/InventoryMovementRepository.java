
package com.salessavvy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salessavvy.entity.InventoryMovement;

public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement>
    findByProduct_IdOrderByCreatedAtDesc(Long productId);

    List<InventoryMovement>
    findAllByOrderByCreatedAtDesc();
}
