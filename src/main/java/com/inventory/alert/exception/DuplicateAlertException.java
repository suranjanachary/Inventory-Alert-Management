package com.inventory.alert.exception;

/**
 * Guards the rule "only one PENDING LOW_STOCK alert per product".
 */
public class DuplicateAlertException extends BusinessException {

    public DuplicateAlertException(Long productId) {
        super("A pending LOW_STOCK alert already exists for product id: " + productId);
    }
}
