package com.example.order.exception;

/**
 * Exception thrown when external service calls fail
 */
public class ExternalServiceException extends OrderException {
    public ExternalServiceException(String message) {
        super(message, 503);
    }

    public ExternalServiceException(String message, int statusCode) {
        super(message, statusCode);
    }

    public ExternalServiceException(String message, int statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }
}
