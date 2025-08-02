package com.logistics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when inventory is insufficient
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientInventoryException extends RuntimeException {
    private final String productSku;
    private final Integer availableQuantity;
    private final Integer requestedQuantity;
    
    public InsufficientInventoryException(String message) {
        super(message);
        this.productSku = null;
        this.availableQuantity = null;
        this.requestedQuantity = null;
    }
    
    public InsufficientInventoryException(String productSku, Integer availableQuantity, Integer requestedQuantity) {
        super(String.format("Insufficient inventory for product %s. Available: %d, Requested: %d", 
              productSku, availableQuantity, requestedQuantity));
        this.productSku = productSku;
        this.availableQuantity = availableQuantity;
        this.requestedQuantity = requestedQuantity;
    }
    
    public String getProductSku() { return productSku; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public Integer getRequestedQuantity() { return requestedQuantity; }
}