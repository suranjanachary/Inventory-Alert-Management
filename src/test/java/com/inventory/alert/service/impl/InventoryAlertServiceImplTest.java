package com.inventory.alert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.inventory.alert.dto.response.InventoryAlertResponse;
import com.inventory.alert.entity.InventoryAlert;
import com.inventory.alert.entity.Product;
import com.inventory.alert.enums.AlertStatus;
import com.inventory.alert.enums.AlertType;
import com.inventory.alert.mapper.InventoryAlertMapper;
import com.inventory.alert.repository.InventoryAlertRepository;
import com.inventory.alert.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryAlertServiceImplTest {

    @Mock
    private InventoryAlertRepository alertRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryAlertMapper alertMapper;

    @InjectMocks
    private InventoryAlertServiceImpl alertService;

    @Test
    void markAlertSent_whenPending_updatesStatus() {
        Product product = new Product("SKU", "P", null, BigDecimal.ONE, 1, 1, true);
        product.setId(1L);
        InventoryAlert alert = new InventoryAlert(product, AlertType.LOW_STOCK, AlertStatus.PENDING, "msg");
        alert.setId(9L);

        when(alertRepository.findById(9L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(alert)).thenReturn(alert);
        when(alertMapper.toResponse(alert)).thenReturn(InventoryAlertResponse.builder()
                .id(9L)
                .status(AlertStatus.SENT)
                .build());

        Optional<InventoryAlertResponse> result = alertService.markAlertSent(9L);

        assertThat(result).isPresent();
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.SENT);
    }

    @Test
    void markAlertSent_whenAlreadySent_returnsEmpty() {
        Product product = new Product("SKU", "P", null, BigDecimal.ONE, 1, 1, true);
        product.setId(1L);
        InventoryAlert alert = new InventoryAlert(product, AlertType.LOW_STOCK, AlertStatus.SENT, "msg");
        alert.setId(9L);
        when(alertRepository.findById(9L)).thenReturn(Optional.of(alert));

        assertThat(alertService.markAlertSent(9L)).isEmpty();
    }

    @Test
    void ensurePendingLowStockAlert_whenExists_returnsFalse() {
        when(alertRepository.existsByProductIdAndAlertTypeAndStatus(
                        1L, AlertType.LOW_STOCK, AlertStatus.PENDING))
                .thenReturn(true);

        assertThat(alertService.ensurePendingLowStockAlert(1L, "msg")).isFalse();
    }
}
