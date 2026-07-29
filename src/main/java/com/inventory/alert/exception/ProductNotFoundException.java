package com.inventory.alert.exception;

/**
 * Raised when a product id/SKU cannot be resolved. Distinct from inactive/stock errors
 * so callers can return 404 vs 409/400.
 */
public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }

    public ProductNotFoundException(String sku) {
        super("Product not found with SKU: " + sku);
    }
}
