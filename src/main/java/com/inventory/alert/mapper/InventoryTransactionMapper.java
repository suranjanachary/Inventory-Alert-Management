package com.inventory.alert.mapper;

import com.inventory.alert.dto.response.InventoryTransactionResponse;
import com.inventory.alert.entity.InventoryTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryTransactionMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSku", source = "product.sku")
    InventoryTransactionResponse toResponse(InventoryTransaction transaction);
}
