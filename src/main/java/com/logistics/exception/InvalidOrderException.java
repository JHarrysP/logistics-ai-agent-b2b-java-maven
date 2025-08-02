package com.logistics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when order validation fails
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOrderException extends RuntimeException {
    private final String orderField;
    private final Object rejectedValue;
    
    public InvalidOrderException(String message) {
        super(message);
        this.orderField = null;
        this.rejectedValue = null;
    }
    
    public InvalidOrderException(String message, String orderField, Object rejectedValue) {
        super(message);
        this.orderField = orderField;
        this.rejectedValue = rejectedValue;
    }
    
    public InvalidOrderException(String message, Throwable cause) {
        super(message, cause);
        this.orderField = null;
        this.rejectedValue = null;
    }
    
    public String getOrderField() { return orderField; }
    public Object getRejectedValue() { return rejectedValue; }
}