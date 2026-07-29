package com.inventory.alert.service;

import com.inventory.alert.dto.request.ProductCreateRequest;
import com.inventory.alert.dto.request.ProductUpdateRequest;
import com.inventory.alert.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest request);

    ProductResponse updateProduct(Long id, ProductUpdateRequest request);

    ProductResponse softDeleteProduct(Long id);

    ProductResponse getProductById(Long id);

    ProductResponse getProductBySku(String sku);

    Page<ProductResponse> searchProducts(String name, Pageable pageable);

    Page<ProductResponse> listProducts(Pageable pageable);

    Page<ProductResponse> listActiveProducts(Pageable pageable);
}
