package com.inventory.alert.mapper;

import com.inventory.alert.dto.request.ProductCreateRequest;
import com.inventory.alert.dto.response.ProductResponse;
import com.inventory.alert.entity.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    @BeanMapping(ignoreByDefault = false)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Product toEntity(ProductCreateRequest request);
}
