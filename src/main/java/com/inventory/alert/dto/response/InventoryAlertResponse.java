package com.inventory.alert.dto.response;

import com.inventory.alert.enums.AlertStatus;
import com.inventory.alert.enums.AlertType;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryAlertResponse {

    private Long id;
    private Long productId;
    private String productSku;
    private AlertType alertType;
    private AlertStatus status;
    private String message;
    private Instant createdAt;
    private Instant resolvedAt;
}
