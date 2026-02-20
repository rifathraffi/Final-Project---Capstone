package com.example.order.exception;

/**
 * Exception thrown when order data is invalid
 */
public class InvalidOrderDataException extends OrderException {
    public InvalidOrderDataException(String message) {
        super(message, 400);
    }

    public InvalidOrderDataException(String message, Throwable cause) {
        super(message, 400, cause);
    }
}
