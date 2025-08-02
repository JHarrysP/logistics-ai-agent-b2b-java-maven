package com.logistics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when order is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderNotFoundException extends RuntimeException {
    private final Long orderId;
    
    public OrderNotFoundException(Long orderId) {
        super("Order not found with ID: " + orderId);
        this.orderId = orderId;
    }
    
    public OrderNotFoundException(String message) {
        super(message);
        this.orderId = null;
    }
    
    public Long getOrderId() { return orderId; }
}