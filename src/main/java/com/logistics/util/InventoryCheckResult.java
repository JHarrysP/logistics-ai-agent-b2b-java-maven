package com.logistics.util;

/**
 * Result object for inventory availability checks
 */
public class InventoryCheckResult {
    private final boolean available;
    private final String message;
    
    public InventoryCheckResult(boolean available, String message) {
        this.available = available;
        this.message = message;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return "InventoryCheckResult{" +
                "available=" + available +
                ", message='" + message + '\'' +
                '}';
    }
}