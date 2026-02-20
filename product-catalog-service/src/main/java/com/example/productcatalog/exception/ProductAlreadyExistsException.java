package com.example.productcatalog.exception;

/**
 * Exception thrown when attempting to create a product with a duplicate SKU
 */
public class ProductAlreadyExistsException extends ProductException {
    public ProductAlreadyExistsException(String sku) {
        super("Product with SKU '" + sku + "' already exists", 409);
    }

    public ProductAlreadyExistsException(String message, int statusCode) {
        super(message, statusCode);
    }
}
