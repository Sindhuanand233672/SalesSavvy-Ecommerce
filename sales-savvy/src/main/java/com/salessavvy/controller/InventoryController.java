
package com.salessavvy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.salessavvy.dto.InventoryMovementResponse;
import com.salessavvy.dto.InventoryResponse;
import com.salessavvy.dto.LowStockProductResponse;
import com.salessavvy.dto.StockAdjustmentRequest;
import com.salessavvy.service.InventoryService;

@RestController
@RequestMapping("/api/inventory")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // Add or remove stock
    @PostMapping("/products/{productId}/adjust")
    public ResponseEntity<InventoryResponse> adjustStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request) {

        return new ResponseEntity<>(
                inventoryService.adjustStock(productId, request),
                HttpStatus.OK);
    }

    // Get all active product inventory
    @GetMapping("/products")
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {

        return ResponseEntity.ok(
                inventoryService.getAllInventory());
    }

    // Get low-stock products
    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockProductResponse>>
            getLowStockProducts(
                    @RequestParam(defaultValue = "5") Integer threshold) {

        return ResponseEntity.ok(
                inventoryService.getLowStockProducts(threshold));
    }

    // Get stock movement history for a product
    @GetMapping("/products/{productId}/history")
    public ResponseEntity<List<InventoryMovementResponse>>
            getProductMovementHistory(
                    @PathVariable Long productId) {

        return ResponseEntity.ok(
                inventoryService.getProductMovementHistory(productId));
    }

    // Get complete inventory movement history
    @GetMapping("/history")
    public ResponseEntity<List<InventoryMovementResponse>>
            getAllMovementHistory() {

        return ResponseEntity.ok(
                inventoryService.getAllMovementHistory());
    }
}
