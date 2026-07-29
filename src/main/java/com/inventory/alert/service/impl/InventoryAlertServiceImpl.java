package com.inventory.alert.service.impl;

import com.inventory.alert.dto.response.InventoryAlertResponse;
import com.inventory.alert.entity.InventoryAlert;
import com.inventory.alert.entity.Product;
import com.inventory.alert.enums.AlertStatus;
import com.inventory.alert.enums.AlertType;
import com.inventory.alert.exception.AlertAlreadyResolvedException;
import com.inventory.alert.exception.AlertNotFoundException;
import com.inventory.alert.exception.ProductNotFoundException;
import com.inventory.alert.mapper.InventoryAlertMapper;
import com.inventory.alert.repository.InventoryAlertRepository;
import com.inventory.alert.repository.ProductRepository;
import com.inventory.alert.service.InventoryAlertService;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryAlertServiceImpl implements InventoryAlertService {

    private final InventoryAlertRepository alertRepository;
    private final ProductRepository productRepository;
    private final InventoryAlertMapper alertMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryAlertResponse> getPendingAlerts(Pageable pageable) {
        // idx_inv_alerts_status — scheduler/ops inbox.
        return alertRepository
                .findByStatus(AlertStatus.PENDING, pageable)
                .map(alertMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryAlertResponse> getAlerts(Pageable pageable) {
        return alertRepository.findAllWithProduct(pageable).map(alertMapper::toResponse);
    }

    /**
     * Write TX: status flip + resolvedAt must persist together.
     */
    @Override
    @Transactional
    public InventoryAlertResponse resolveAlert(Long alertId) {
        InventoryAlert alert = alertRepository
                .findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));
        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new AlertAlreadyResolvedException(alertId);
        }
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(Instant.now());
        InventoryAlert saved = alertRepository.save(alert);
        log.info("Alert resolved id={} productId={}", saved.getId(), productId(saved));
        return alertMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryAlertResponse> getAlertsByProduct(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        return alertRepository.findByProductId(productId, pageable).map(alertMapper::toResponse);
    }

    /**
     * Participates in caller's TX (REQUIRED). existsBy... avoids loading rows when an alert
     * already exists; getReferenceById sets the FK without a full product select.
     */
    @Override
    @Transactional
    public boolean ensurePendingLowStockAlert(Long productId, String message) {
        if (alertRepository.existsByProductIdAndAlertTypeAndStatus(
                productId, AlertType.LOW_STOCK, AlertStatus.PENDING)) {
            return false;
        }
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        Product productRef = productRepository.getReferenceById(productId);
        InventoryAlert alert =
                new InventoryAlert(productRef, AlertType.LOW_STOCK, AlertStatus.PENDING, message);
        alertRepository.save(alert);
        log.info("Low stock alert created productId={}", productId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryAlertResponse> findPendingAlertsForDispatch(Pageable pageable) {
        return alertRepository
                .findByStatus(AlertStatus.PENDING, pageable)
                .map(alertMapper::toResponse);
    }

    /**
     * Only transitions PENDING → SENT. Concurrent schedulers: second call sees non-PENDING and no-ops.
     */
    @Override
    @Transactional
    public Optional<InventoryAlertResponse> markAlertSent(Long alertId) {
        InventoryAlert alert = alertRepository
                .findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException(alertId));
        if (alert.getStatus() != AlertStatus.PENDING) {
            return Optional.empty();
        }
        alert.setStatus(AlertStatus.SENT);
        InventoryAlert saved = alertRepository.save(alert);
        log.info("Alert marked SENT id={} productId={}", saved.getId(), productId(saved));
        return Optional.of(alertMapper.toResponse(saved));
    }

    private static Long productId(InventoryAlert alert) {
        return alert.getProduct().getId();
    }
}
