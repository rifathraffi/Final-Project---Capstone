package com.example.productcatalog.exception;

/**
 * Base exception for product-related errors
 */
public class ProductException extends RuntimeException {
    private final int statusCode;

    public ProductException(String message) {
        this(message, 500);
    }

    public ProductException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ProductException(String message, Throwable cause) {
        this(message, 500, cause);
    }

    public ProductException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
