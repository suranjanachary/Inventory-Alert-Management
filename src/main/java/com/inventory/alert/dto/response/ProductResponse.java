package com.inventory.alert.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer availableQuantity;
    private Integer minimumQuantity;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
}
