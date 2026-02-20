package com.example.productcatalog.exception;

/**
 * Exception thrown when a product is not found
 */
public class ProductNotFoundException extends ProductException {
    public ProductNotFoundException(String message) {
        super(message, 404);
    }

    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id, 404);
    }

    public ProductNotFoundException(String field, String value) {
        super("Product not found with " + field + ": " + value, 404);
    }
}
