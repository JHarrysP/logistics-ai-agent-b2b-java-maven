package com.logistics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when shipment operations fail
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ShipmentException extends RuntimeException {
    private final Long shipmentId;
    private final String currentStatus;
    
    public ShipmentException(String message) {
        super(message);
        this.shipmentId = null;
        this.currentStatus = null;
    }
    
    public ShipmentException(Long shipmentId, String currentStatus, String message) {
        super(String.format("Shipment operation failed for shipment %d (status: %s): %s", 
              shipmentId, currentStatus, message));
        this.shipmentId = shipmentId;
        this.currentStatus = currentStatus;
    }
    
    public Long getShipmentId() { return shipmentId; }
    public String getCurrentStatus() { return currentStatus; }
}