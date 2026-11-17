
package com.salessavvy.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.dto.*;
import com.salessavvy.entity.*;
import com.salessavvy.repository.InventoryMovementRepository;
import com.salessavvy.repository.ProductRepository;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;

    public InventoryServiceImpl(
            ProductRepository productRepository,
            InventoryMovementRepository movementRepository) {

        this.productRepository = productRepository;
        this.movementRepository = movementRepository;
    }

    @Override
    @Transactional
    public InventoryResponse adjustStock(
            Long productId,
            StockAdjustmentRequest request) {

    	Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"));

        if (Boolean.FALSE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot adjust stock for an inactive product");
        }

        int previousStock = product.getStockQuantity();
        int quantity = request.getQuantity();
        if (quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stock adjustment quantity must be positive");
        }
        int newStock;

        if (request.getMovementType()
                == InventoryMovementType.STOCK_ADDED) {

            newStock = Math.addExact(previousStock, quantity);

        } else if (request.getMovementType()
                == InventoryMovementType.STOCK_REMOVED) {

            if (quantity > previousStock) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock available");
            }

            newStock = previousStock - quantity;

        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid stock movement type");
        }

        product.setStockQuantity(newStock);
        productRepository.save(product);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String performedBy = authentication.getName();

        InventoryMovement movement = new InventoryMovement();

        movement.setProduct(product);
        movement.setMovementType(request.getMovementType());
        movement.setQuantity(quantity);
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setReason(request.getReason());
        movement.setPerformedBy(performedBy);

        movementRepository.save(movement);

        return new InventoryResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                newStock,
                "Stock updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {

        return productRepository.findAll().stream()
                .filter(product ->
                        !Boolean.FALSE.equals(product.getActive()))
                .map(product -> new InventoryResponse(
                        product.getId(),
                        product.getName(),
                        product.getSku(),
                        product.getStockQuantity(),
                        "Inventory details"))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockProductResponse> getLowStockProducts(
            Integer threshold) {

        if (threshold == null || threshold < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stock threshold cannot be negative");
        }

        return productRepository.findAll().stream()
                .filter(product ->
                        !Boolean.FALSE.equals(product.getActive()))
                .filter(product ->
                        product.getStockQuantity() <= threshold)
                .map(product -> new LowStockProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getSku(),
                        product.getStockQuantity()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> getProductMovementHistory(
            Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found");
        }

        return movementRepository
                .findByProduct_IdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> getAllMovementHistory() {

        return movementRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private InventoryMovementResponse convertToResponse(
            InventoryMovement movement) {

        return new InventoryMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getName(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getPreviousStock(),
                movement.getNewStock(),
                movement.getReason(),
                movement.getPerformedBy(),
                movement.getCreatedAt());
    }
}
