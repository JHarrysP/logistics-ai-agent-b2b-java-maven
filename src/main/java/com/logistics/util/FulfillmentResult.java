package com.logistics.util;

/**
 * Result object for order fulfillment operations
 */
public class FulfillmentResult {
    private final boolean successful;
    private final String message;
    
    public FulfillmentResult(boolean successful, String message) {
        this.successful = successful;
        this.message = message;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return "FulfillmentResult{" +
                "successful=" + successful +
                ", message='" + message + '\'' +
                '}';
    }
}