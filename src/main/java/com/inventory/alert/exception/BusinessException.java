package com.inventory.alert.exception;

/**
 * Base type for domain failures that map cleanly to API error responses later.
 */
public abstract class BusinessException extends RuntimeException {

    protected BusinessException(String message) {
        super(message);
    }
}
