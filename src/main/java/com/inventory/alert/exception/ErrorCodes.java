package com.inventory.alert.exception;

/**
 * Machine-readable codes for clients and support (optional on ErrorResponse).
 */
public final class ErrorCodes {

    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    public static final String DUPLICATE_SKU = "DUPLICATE_SKU";
    public static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    public static final String DUPLICATE_ALERT = "DUPLICATE_ALERT";
    public static final String INACTIVE_PRODUCT = "INACTIVE_PRODUCT";
    public static final String ALERT_NOT_FOUND = "ALERT_NOT_FOUND";
    public static final String ALERT_ALREADY_RESOLVED = "ALERT_ALREADY_RESOLVED";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
    public static final String OPTIMISTIC_LOCK = "OPTIMISTIC_LOCK";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";

    private ErrorCodes() {
    }
}
