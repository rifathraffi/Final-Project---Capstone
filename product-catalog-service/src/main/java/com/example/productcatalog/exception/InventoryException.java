package com.example.productcatalog.exception;

/**
 * Exception thrown when inventory operations fail
 */
public class InventoryException extends ProductException {
    public InventoryException(String message) {
        super(message, 400);
    }

    public InventoryException(String message, int statusCode) {
        super(message, statusCode);
    }

    public InventoryException(String message, int statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }
}
