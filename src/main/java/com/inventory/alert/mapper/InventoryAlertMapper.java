package com.inventory.alert.mapper;

import com.inventory.alert.dto.response.InventoryAlertResponse;
import com.inventory.alert.entity.InventoryAlert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryAlertMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSku", source = "product.sku")
    InventoryAlertResponse toResponse(InventoryAlert alert);
}
