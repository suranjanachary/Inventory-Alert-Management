package com.inventory.alert.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductUpdateRequest {

    @Size(max = 64)
    private String sku;

    @Size(max = 200)
    private String name;

    @Size(max = 2000)
    private String description;

    @DecimalMin("0.00")
    private BigDecimal price;

    @Min(0)
    private Integer availableQuantity;

    @Min(0)
    private Integer minimumQuantity;

    private Boolean active;
}
