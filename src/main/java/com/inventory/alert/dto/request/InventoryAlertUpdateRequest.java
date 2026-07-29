package com.inventory.alert.dto.request;

import com.inventory.alert.enums.AlertStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Alerts are mostly status-driven. Message may be clarified; product/type are immutable after create.
 */
@Getter
@Setter
public class InventoryAlertUpdateRequest {

    private AlertStatus status;

    @Size(max = 1000)
    private String message;
}
