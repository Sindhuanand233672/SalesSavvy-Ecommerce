
package com.salessavvy.service;

import java.util.List;

import com.salessavvy.dto.InventoryMovementResponse;
import com.salessavvy.dto.InventoryResponse;
import com.salessavvy.dto.LowStockProductResponse;
import com.salessavvy.dto.StockAdjustmentRequest;

public interface InventoryService {

    InventoryResponse adjustStock(
            Long productId,
            StockAdjustmentRequest request);

    List<InventoryResponse> getAllInventory();

    List<LowStockProductResponse> getLowStockProducts(
            Integer threshold);

    List<InventoryMovementResponse> getProductMovementHistory(
            Long productId);

    List<InventoryMovementResponse> getAllMovementHistory();
}

