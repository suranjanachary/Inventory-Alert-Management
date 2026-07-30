package com.inventory.alert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventory.alert.dto.request.ProductCreateRequest;
import com.inventory.alert.dto.response.ProductResponse;
import com.inventory.alert.entity.Product;
import com.inventory.alert.exception.DuplicateSkuException;
import com.inventory.alert.exception.ProductNotFoundException;
import com.inventory.alert.mapper.ProductMapper;
import com.inventory.alert.repository.ProductRepository;
import com.inventory.alert.service.support.RequestValidator;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private RequestValidator requestValidator;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_whenSkuExists_throwsDuplicate() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setSku("SKU-1");
        request.setName("Widget");
        request.setPrice(new BigDecimal("1.00"));
        request.setAvailableQuantity(1);
        request.setMinimumQuantity(0);
        when(productRepository.existsBySku("SKU-1")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateSkuException.class);
    }

    @Test
    void createProduct_persistsAndReturnsDto() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setSku("SKU-2");
        request.setName("Gadget");
        request.setPrice(new BigDecimal("2.00"));
        request.setAvailableQuantity(5);
        request.setMinimumQuantity(1);

        Product entity = new Product("SKU-2", "Gadget", null, new BigDecimal("2.00"), 5, 1, true);
        Product saved = new Product("SKU-2", "Gadget", null, new BigDecimal("2.00"), 5, 1, true);
        saved.setId(10L);

        when(productRepository.existsBySku("SKU-2")).thenReturn(false);
        when(productMapper.toEntity(request)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(ProductResponse.builder()
                .id(10L)
                .sku("SKU-2")
                .name("Gadget")
                .build());

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getSku()).isEqualTo("SKU-2");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_whenMissing_throws() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(5L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void softDeleteProduct_setsInactive() {
        Product product = new Product("SKU-3", "Item", null, BigDecimal.ONE, 1, 0, true);
        product.setId(3L);
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(ProductResponse.builder()
                .id(3L)
                .active(false)
                .build());

        ProductResponse response = productService.softDeleteProduct(3L);

        assertThat(product.getActive()).isFalse();
        assertThat(response.getActive()).isFalse();
    }
}
