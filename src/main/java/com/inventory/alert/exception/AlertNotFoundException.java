package com.inventory.alert.exception;

/**
 * Alert id not found when resolving or fetching.
 */
public class AlertNotFoundException extends BusinessException {

    public AlertNotFoundException(Long id) {
        super("Inventory alert not found with id: " + id);
    }
}
