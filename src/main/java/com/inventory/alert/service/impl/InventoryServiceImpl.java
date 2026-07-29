package com.inventory.alert.service.impl;

import com.inventory.alert.dto.request.StockMutationRequest;
import com.inventory.alert.dto.response.InventoryTransactionResponse;
import com.inventory.alert.entity.InventoryTransaction;
import com.inventory.alert.entity.Product;
import com.inventory.alert.enums.TransactionType;
import com.inventory.alert.exception.InactiveProductException;
import com.inventory.alert.exception.InsufficientStockException;
import com.inventory.alert.exception.ProductNotFoundException;
import com.inventory.alert.mapper.InventoryTransactionMapper;
import com.inventory.alert.repository.InventoryTransactionRepository;
import com.inventory.alert.repository.ProductRepository;
import com.inventory.alert.service.InventoryAlertService;
import com.inventory.alert.service.InventoryService;
import com.inventory.alert.service.support.RequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryTransactionMapper transactionMapper;
    private final InventoryAlertService inventoryAlertService;
    private final RequestValidator requestValidator;

    /**
     * Write TX spanning product stock update + ledger insert.
     * Atomic so a failed ledger write cannot leave stock increased alone.
     */
    @Override
    @Transactional
    public InventoryTransactionResponse purchaseStock(StockMutationRequest request) {
        requestValidator.validate(request);
        Product product = loadProduct(request.getProductId());
        int before = product.getAvailableQuantity();
        int after = before + request.getQuantity();
        product.setAvailableQuantity(after);
        // Dirty Product flushes with @Version check on commit.
        productRepository.save(product);
        InventoryTransaction txn = saveLedger(
                product, TransactionType.PURCHASE, request.getQuantity(), before, after, request.getRemarks());
        log.info(
                "Purchase completed productId={} qty={} before={} after={}",
                product.getId(),
                request.getQuantity(),
                before,
                after);
        return transactionMapper.toResponse(txn);
    }

    /**
     * Write TX: stock decrement + ledger + optional LOW_STOCK alert must succeed or roll back together.
     */
    @Override
    @Transactional
    public InventoryTransactionResponse sellStock(StockMutationRequest request) {
        requestValidator.validate(request);
        Product product = loadProduct(request.getProductId());
        assertActive(product);
        assertSufficientStock(product, request.getQuantity());

        int before = product.getAvailableQuantity();
        int after = before - request.getQuantity();
        product.setAvailableQuantity(after);
        productRepository.save(product);

        InventoryTransaction txn = saveLedger(
                product, TransactionType.SALE, request.getQuantity(), before, after, request.getRemarks());

        if (after <= product.getMinimumQuantity()) {
            // Joins this TX via Spring's default REQUIRED propagation.
            boolean created = inventoryAlertService.ensurePendingLowStockAlert(
                    product.getId(), buildLowStockMessage(product, after));
            if (created) {
                log.info("Low stock alert created productId={} available={}", product.getId(), after);
            }
        }

        log.info(
                "Sale completed productId={} qty={} before={} after={}",
                product.getId(),
                request.getQuantity(),
                before,
                after);
        return transactionMapper.toResponse(txn);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponse> getInventoryHistory(Long productId, Pageable pageable) {
        // Ensure 404 for unknown products before an empty page is mistaken for "no history".
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        // Uses idx_inv_txn_product_created; product association loads for SKU mapping.
        return transactionRepository
                .findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(transactionMapper::toResponse);
    }

    private Product loadProduct(Long productId) {
        return productRepository
                .findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void assertActive(Product product) {
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new InactiveProductException(product.getId());
        }
    }

    private void assertSufficientStock(Product product, int requested) {
        if (product.getAvailableQuantity() < requested) {
            throw new InsufficientStockException(
                    product.getId(), requested, product.getAvailableQuantity());
        }
    }

    private InventoryTransaction saveLedger(
            Product product,
            TransactionType type,
            int quantity,
            int before,
            int after,
            String remarks) {
        InventoryTransaction txn =
                new InventoryTransaction(product, type, quantity, before, after, remarks);
        return transactionRepository.save(txn);
    }

    private String buildLowStockMessage(Product product, int available) {
        return "LOW_STOCK: product SKU "
                + product.getSku()
                + " available="
                + available
                + " minimum="
                + product.getMinimumQuantity();
    }
}
