package com.example.order.exception;

/**
 * Exception thrown when an order is not found
 */
public class OrderNotFoundException extends OrderException {
    public OrderNotFoundException(String message) {
        super(message, 404);
    }

    public OrderNotFoundException(Long id) {
        super("Order not found with id: " + id, 404);
    }

    public OrderNotFoundException(String field, String value) {
        super("Order not found with " + field + ": " + value, 404);
    }
}
