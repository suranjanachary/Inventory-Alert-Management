package com.inventory.alert.service;

import com.inventory.alert.dto.request.StockMutationRequest;
import com.inventory.alert.dto.response.InventoryTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    InventoryTransactionResponse purchaseStock(StockMutationRequest request);

    InventoryTransactionResponse sellStock(StockMutationRequest request);

    Page<InventoryTransactionResponse> getInventoryHistory(Long productId, Pageable pageable);
}
