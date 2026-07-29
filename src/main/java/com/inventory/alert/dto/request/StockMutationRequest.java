package com.inventory.alert.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Shared payload for purchase and sell operations. Transaction type is chosen by the service method.
 */
@Getter
@Setter
public class StockMutationRequest {

    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @Size(max = 500)
    private String remarks;
}
