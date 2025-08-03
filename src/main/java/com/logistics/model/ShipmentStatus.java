package com.logistics.model;

/**
 * Shipment status enumeration representing the logistics delivery lifecycle
 */
public enum ShipmentStatus {
    SCHEDULED("Shipment scheduled for pickup"),
    LOADING("Currently loading items onto truck"),
    LOADED("All items loaded and ready for departure"),
    IN_TRANSIT("Shipment is in transit to destination"),
    DELIVERED("Shipment delivered successfully"),
    CANCELLED("Shipment cancelled");
    
    private final String description;
    
    ShipmentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}