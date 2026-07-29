package com.inventory.alert.repository;

import com.inventory.alert.entity.InventoryAlert;
import com.inventory.alert.enums.AlertStatus;
import com.inventory.alert.enums.AlertType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryAlertRepository extends JpaRepository<InventoryAlert, Long> {

    Page<InventoryAlert> findByStatus(AlertStatus status, Pageable pageable);

    Page<InventoryAlert> findByProductId(Long productId, Pageable pageable);

    Page<InventoryAlert> findByProductIdAndStatus(Long productId, AlertStatus status, Pageable pageable);

    List<InventoryAlert> findByStatusAndAlertType(AlertStatus status, AlertType alertType);

    /**
     * Used by services to avoid duplicate open LOW_STOCK alerts for the same product.
     */
    Optional<InventoryAlert> findFirstByProductIdAndAlertTypeAndStatus(
            Long productId, AlertType alertType, AlertStatus status);

    boolean existsByProductIdAndAlertTypeAndStatus(
            Long productId, AlertType alertType, AlertStatus status);
}
