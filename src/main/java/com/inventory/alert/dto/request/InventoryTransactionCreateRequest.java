package com.inventory.alert.dto.request;

import com.inventory.alert.enums.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Ledger create payload. quantityBefore/quantityAfter are computed in the service layer
 * when stock is applied — clients must not set them.
 */
@Getter
@Setter
public class InventoryTransactionCreateRequest {

    @NotNull
    private Long productId;

    @NotNull
    private TransactionType transactionType;

    @NotNull
    @Min(1)
    private Integer quantity;

    @Size(max = 500)
    private String remarks;
}
