package com.inventory.alert.repository;

import com.inventory.alert.entity.InventoryTransaction;
import com.inventory.alert.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    @EntityGraph(attributePaths = "product")
    Page<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    @EntityGraph(attributePaths = "product")
    Page<InventoryTransaction> findByProductIdAndTransactionTypeOrderByCreatedAtDesc(
            Long productId, TransactionType transactionType, Pageable pageable);
}
