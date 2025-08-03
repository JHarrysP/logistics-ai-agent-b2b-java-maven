package com.logistics.model;

/**
 * Order status enumeration representing the lifecycle of an order
 */
public enum OrderStatus {
    RECEIVED("Order received and waiting for validation"),
    VALIDATED("Order validated successfully"),
    INVENTORY_CHECKED("Inventory availability confirmed"),
    FULFILLED("Order fulfilled and inventory reserved"),
    READY_FOR_PICKUP("Order ready for warehouse pickup"),
    IN_TRANSIT("Order is being delivered"),
    DELIVERED("Order delivered successfully"),
    CANCELLED("Order cancelled");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}