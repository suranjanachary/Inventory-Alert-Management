package com.inventory.alert.service;

import com.inventory.alert.dto.response.InventoryAlertResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryAlertService {

    Page<InventoryAlertResponse> getPendingAlerts(Pageable pageable);

    Page<InventoryAlertResponse> getAlerts(Pageable pageable);

    InventoryAlertResponse resolveAlert(Long alertId);

    Page<InventoryAlertResponse> getAlertsByProduct(Long productId, Pageable pageable);

    /**
     * Creates a PENDING LOW_STOCK alert when none exists for the product.
     * Invoked from inventory mutations inside the same transaction.
     *
     * @return true if a new alert was created
     */
    boolean ensurePendingLowStockAlert(Long productId, String message);
}
