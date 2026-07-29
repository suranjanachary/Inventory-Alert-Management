package com.inventory.alert.exception;

/**
 * Sales (and other catalog operations) must not mutate inactive products.
 */
public class InactiveProductException extends BusinessException {

    public InactiveProductException(Long productId) {
        super("Product is inactive and cannot be used for this operation. id: " + productId);
    }
}
