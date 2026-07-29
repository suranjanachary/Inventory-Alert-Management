package com.inventory.alert.dto.response;

import com.inventory.alert.enums.TransactionType;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryTransactionResponse {

    private Long id;
    private Long productId;
    private String productSku;
    private TransactionType transactionType;
    private Integer quantity;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private String remarks;
    private Instant createdAt;
}
