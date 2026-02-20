package com.example.order.exception;

/**
 * Exception thrown for invalid order state transitions
 */
public class InvalidOrderStateException extends OrderException {
    public InvalidOrderStateException(String message) {
        super(message, 400);
    }

    public InvalidOrderStateException(String currentState, String requestedState) {
        super("Cannot transition order from " + currentState + " to " + requestedState, 400);
    }
}
