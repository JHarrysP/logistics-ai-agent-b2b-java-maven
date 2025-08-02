package com.logistics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when product is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {
    private final String sku;
    
    public ProductNotFoundException(String sku) {
        super("Product not found with SKU: " + sku);
        this.sku = sku;
    }
    
    public ProductNotFoundException(String message, String sku) {
        super(message);
        this.sku = sku;
    }
    
    public String getSku() { return sku; }
}