package com.inventory.alert.exception;

/**
 * Enforces the SKU uniqueness business rule before hitting the DB unique constraint,
 * so the API can return a clear conflict message instead of a raw DataIntegrityViolation.
 */
public class DuplicateSkuException extends BusinessException {

    public DuplicateSkuException(String sku) {
        super("An active or inactive product already exists with SKU: " + sku);
    }
}
