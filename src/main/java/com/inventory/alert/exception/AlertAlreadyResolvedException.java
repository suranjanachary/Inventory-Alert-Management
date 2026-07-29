package com.inventory.alert.exception;

/**
 * Idempotent resolve guard — RESOLVED alerts must not be resolved again.
 */
public class AlertAlreadyResolvedException extends BusinessException {

    public AlertAlreadyResolvedException(Long id) {
        super("Inventory alert is already resolved. id: " + id);
    }
}
