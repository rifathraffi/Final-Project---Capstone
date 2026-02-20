package com.example.order.exception;

/**
 * Base exception for order-related errors
 */
public class OrderException extends RuntimeException {
    private final int statusCode;

    public OrderException(String message) {
        this(message, 500);
    }

    public OrderException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public OrderException(String message, Throwable cause) {
        this(message, 500, cause);
    }

    public OrderException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
