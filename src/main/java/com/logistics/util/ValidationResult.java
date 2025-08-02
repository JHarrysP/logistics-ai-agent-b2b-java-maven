package com.logistics.util;

/**
 * Result object for order validation operations
 */
public class ValidationResult {
    private final boolean valid;
    private final String reason;
    
    public ValidationResult(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", reason='" + reason + '\'' +
                '}';
    }
}