package com.logistics.util;

/**
 * Container for AI-generated warehouse picking instructions
 */
public class WarehouseInstructions {
    private final String instructions;
    private final boolean requiresSpecialHandling;
    private final int estimatedPickingTime; // in minutes
    
    public WarehouseInstructions(String instructions, boolean requiresSpecialHandling, int estimatedPickingTime) {
        this.instructions = instructions;
        this.requiresSpecialHandling = requiresSpecialHandling;
        this.estimatedPickingTime = estimatedPickingTime;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public boolean requiresSpecialHandling() {
        return requiresSpecialHandling;
    }
    
    public int getEstimatedPickingTime() {
        return estimatedPickingTime;
    }
    
    @Override
    public String toString() {
        return "WarehouseInstructions{" +
                "requiresSpecialHandling=" + requiresSpecialHandling +
                ", estimatedPickingTime=" + estimatedPickingTime + " minutes" +
                ", instructionsLength=" + (instructions != null ? instructions.length() : 0) + " chars" +
                '}';
    }
}