package com.example.order.exception;

/**
 * Exception thrown when customer is not found
 */
public class CustomerNotFoundException extends OrderException {
    public CustomerNotFoundException(String message) {
        super(message, 404);
    }

    public CustomerNotFoundException(Long id) {
        super("Customer not found with id: " + id, 404);
    }

    public CustomerNotFoundException(String field, String value) {
        super("Customer not found with " + field + ": " + value, 404);
    }
}
