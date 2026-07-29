package com.inventory.alert.service.impl;

import com.inventory.alert.constants.CacheNames;
import com.inventory.alert.dto.request.ProductCreateRequest;
import com.inventory.alert.dto.request.ProductUpdateRequest;
import com.inventory.alert.dto.response.ProductResponse;
import com.inventory.alert.entity.Product;
import com.inventory.alert.exception.DuplicateSkuException;
import com.inventory.alert.exception.ProductNotFoundException;
import com.inventory.alert.mapper.ProductMapper;
import com.inventory.alert.repository.ProductRepository;
import com.inventory.alert.service.ProductService;
import com.inventory.alert.service.support.RequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final RequestValidator requestValidator;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOW_STOCK, allEntries = true)
    })
    public ProductResponse createProduct(ProductCreateRequest request) {
        requestValidator.validate(request);
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }
        Product product = productMapper.toEntity(request);
        if (product.getActive() == null) {
            product.setActive(Boolean.TRUE);
        }
        Product saved = productRepository.save(product);
        log.info("Product created id={} sku={}", saved.getId(), saved.getSku());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCTS_BY_ID, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.PRODUCTS_BY_SKU, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOW_STOCK, allEntries = true)
    })
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        requestValidator.validate(request);
        Product product = findProductOrThrow(id);
        applySkuChangeIfPresent(product, request.getSku());
        applyMutableFields(product, request);
        Product saved = productRepository.save(product);
        log.info("Product updated id={} sku={}", saved.getId(), saved.getSku());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCTS_BY_ID, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.PRODUCTS_BY_SKU, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOW_STOCK, allEntries = true)
    })
    public ProductResponse softDeleteProduct(Long id) {
        Product product = findProductOrThrow(id);
        product.setActive(Boolean.FALSE);
        Product saved = productRepository.save(product);
        log.info("Product soft-deleted id={} sku={}", saved.getId(), saved.getSku());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.PRODUCTS_BY_ID, key = "#id")
    public ProductResponse getProductById(Long id) {
        return productMapper.toResponse(findProductOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.PRODUCTS_BY_SKU, key = "#sku")
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository
                .findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.PRODUCT_SEARCH,
            key = "#name + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<ProductResponse> searchProducts(String name, Pageable pageable) {
        return productRepository.searchByName(name, pageable).map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> listProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> listActiveProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable).map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheNames.LOW_STOCK,
            key = "#pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<ProductResponse> listLowStockProducts(Pageable pageable) {
        return productRepository.findActiveBelowMinimum(pageable).map(productMapper::toResponse);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private void applySkuChangeIfPresent(Product product, String newSku) {
        if (newSku == null || newSku.equals(product.getSku())) {
            return;
        }
        if (productRepository.existsBySkuAndIdNot(newSku, product.getId())) {
            throw new DuplicateSkuException(newSku);
        }
        product.setSku(newSku);
    }

    private void applyMutableFields(Product product, ProductUpdateRequest request) {
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getAvailableQuantity() != null) {
            product.setAvailableQuantity(request.getAvailableQuantity());
        }
        if (request.getMinimumQuantity() != null) {
            product.setMinimumQuantity(request.getMinimumQuantity());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
    }
}
