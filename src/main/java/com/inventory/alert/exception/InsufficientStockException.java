package com.inventory.alert.exception;

/**
 * Sale attempted for more units than availableQuantity. Prevents negative stock.
 */
public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(Long productId, int requested, int available) {
        super("Insufficient stock for product id "
                + productId
                + ": requested "
                + requested
                + ", available "
                + available);
    }
}
