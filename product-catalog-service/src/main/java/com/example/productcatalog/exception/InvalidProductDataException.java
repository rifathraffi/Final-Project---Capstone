package com.example.productcatalog.exception;

/**
 * Exception thrown when product data is invalid
 */
public class InvalidProductDataException extends ProductException {
    public InvalidProductDataException(String message) {
        super(message, 400);
    }

    public InvalidProductDataException(String message, Throwable cause) {
        super(message, 400, cause);
    }
}
