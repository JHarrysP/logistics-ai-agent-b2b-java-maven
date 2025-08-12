package com.logistics.model;

/**
 * Order status enumeration representing the lifecycle of a logistics order
 */
public enum OrderStatus {
    PENDING("Order pending and awaiting processing"),
    SCHEDULED("Order is scheduled"),
    RECEIVED("Order received and awaiting validation"),
    VALIDATED("Order validated and ready for processing"),
    INVENTORY_CHECKED("Inventory availability verified"), 
    READY_FOR_PICKUP("Order ready for pickup"),
    LOADING("Order is being loaded onto transport vehicle"),
    IN_FULFILLMENT("Order is being fulfilled and prepared for shipment"),
    FULFILLED("Order fulfilled and ready for shipment"),
    IN_TRANSIT("Order shipped and in transit"),
    DELIVERED("Order delivered successfully"),
    CANCELLED("Order cancelled");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return this.name();
    }
}