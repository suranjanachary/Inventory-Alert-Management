package com.inventory.alert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.inventory.alert.service.support.RequestValidator;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryTransactionRepository transactionRepository;
    @Mock
    private InventoryTransactionMapper transactionMapper;
    @Mock
    private InventoryAlertService inventoryAlertService;
    @Mock
    private RequestValidator requestValidator;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("SKU-1", "Widget", null, new BigDecimal("10.00"), 10, 5, true);
        product.setId(1L);
    }

    @Test
    void purchaseStock_increasesQuantityAndWritesLedger() {
        StockMutationRequest request = mutation(1L, 3);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(transactionRepository.save(any(InventoryTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(transactionMapper.toResponse(any())).thenReturn(InventoryTransactionResponse.builder()
                .productId(1L)
                .quantity(3)
                .quantityBefore(10)
                .quantityAfter(13)
                .transactionType(TransactionType.PURCHASE)
                .build());

        InventoryTransactionResponse response = inventoryService.purchaseStock(request);

        assertThat(product.getAvailableQuantity()).isEqualTo(13);
        assertThat(response.getQuantityAfter()).isEqualTo(13);
        ArgumentCaptor<InventoryTransaction> captor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getTransactionType()).isEqualTo(TransactionType.PURCHASE);
        verify(inventoryAlertService, never()).ensurePendingLowStockAlert(any(), any());
    }

    @Test
    void sellStock_whenInsufficient_throws() {
        StockMutationRequest request = mutation(1L, 50);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> inventoryService.sellStock(request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void sellStock_whenInactive_throws() {
        product.setActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> inventoryService.sellStock(mutation(1L, 1)))
                .isInstanceOf(InactiveProductException.class);
    }

    @Test
    void sellStock_whenBelowMinimum_createsAlert() {
        product.setAvailableQuantity(5);
        product.setMinimumQuantity(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryAlertService.ensurePendingLowStockAlert(eq(1L), any())).thenReturn(true);
        when(transactionMapper.toResponse(any())).thenReturn(InventoryTransactionResponse.builder()
                .productId(1L)
                .quantity(1)
                .quantityBefore(5)
                .quantityAfter(4)
                .transactionType(TransactionType.SALE)
                .build());

        inventoryService.sellStock(mutation(1L, 1));

        assertThat(product.getAvailableQuantity()).isEqualTo(4);
        verify(inventoryAlertService).ensurePendingLowStockAlert(eq(1L), any());
    }

    @Test
    void purchaseStock_whenProductMissing_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.purchaseStock(mutation(99L, 1)))
                .isInstanceOf(ProductNotFoundException.class);
    }

    private static StockMutationRequest mutation(Long productId, int qty) {
        StockMutationRequest request = new StockMutationRequest();
        request.setProductId(productId);
        request.setQuantity(qty);
        request.setRemarks("test");
        return request;
    }
}
