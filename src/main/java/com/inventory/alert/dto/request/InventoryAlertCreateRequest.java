package com.inventory.alert.dto.request;

import com.inventory.alert.enums.AlertStatus;
import com.inventory.alert.enums.AlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryAlertCreateRequest {

    @NotNull
    private Long productId;

    @NotNull
    private AlertType alertType;

    /**
     * Defaults to PENDING in the service if omitted; required here for explicit API contracts.
     */
    @NotNull
    private AlertStatus status;

    @NotBlank
    @Size(max = 1000)
    private String message;
}
