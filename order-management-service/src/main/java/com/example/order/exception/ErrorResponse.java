package com.example.order.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response DTO for all exceptions
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        String timestamp,
        String path,
        Map<String, String> validationErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, LocalDateTime.now().toString(), path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> validationErrors) {
        this(status, error, message, LocalDateTime.now().toString(), path, validationErrors);
    }
}
