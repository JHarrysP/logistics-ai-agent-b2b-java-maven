package com.logistics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when order processing fails
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class OrderProcessingException extends RuntimeException {
    private final Long orderId;
    private final String processingStage;
    
    public OrderProcessingException(String message) {
        super(message);
        this.orderId = null;
        this.processingStage = null;
    }
    
    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.orderId = null;
        this.processingStage = null;
    }
    
    public OrderProcessingException(Long orderId, String processingStage, String message) {
        super(String.format("Order processing failed at stage '%s' for order %d: %s", 
              processingStage, orderId, message));
        this.orderId = orderId;
        this.processingStage = processingStage;
    }
    
    public OrderProcessingException(Long orderId, String processingStage, String message, Throwable cause) {
        super(String.format("Order processing failed at stage '%s' for order %d: %s", 
              processingStage, orderId, message), cause);
        this.orderId = orderId;
        this.processingStage = processingStage;
    }
    
    public Long getOrderId() { return orderId; }
    public String getProcessingStage() { return processingStage; }
}