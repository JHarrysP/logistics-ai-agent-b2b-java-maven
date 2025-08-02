// ============= SHIPMENT INFO DTO =============
package com.logistics.dto;

import java.time.LocalDateTime;

/**
 * DTO for shipment information within order status
 */
public class ShipmentInfo {
    private Long shipmentId;
    private String truckId;
    private String driverId;
    private String status;
    private LocalDateTime scheduledPickup;
    private LocalDateTime actualPickup;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private Boolean requiresSpecialHandling;
    
    // Constructors
    public ShipmentInfo() {}
    
    public ShipmentInfo(Long shipmentId, String truckId, String driverId, String status,
                       LocalDateTime scheduledPickup, LocalDateTime estimatedDelivery) {
        this.shipmentId = shipmentId;
        this.truckId = truckId;
        this.driverId = driverId;
        this.status = status;
        this.scheduledPickup = scheduledPickup;
        this.estimatedDelivery = estimatedDelivery;
    }
    
    // Getters and setters
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    
    public String getTruckId() { return truckId; }
    public void setTruckId(String truckId) { this.truckId = truckId; }
    
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getScheduledPickup() { return scheduledPickup; }
    public void setScheduledPickup(LocalDateTime scheduledPickup) { this.scheduledPickup = scheduledPickup; }
    
    public LocalDateTime getActualPickup() { return actualPickup; }
    public void setActualPickup(LocalDateTime actualPickup) { this.actualPickup = actualPickup; }
    
    public LocalDateTime getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    
    public LocalDateTime getActualDelivery() { return actualDelivery; }
    public void setActualDelivery(LocalDateTime actualDelivery) { this.actualDelivery = actualDelivery; }
    
    public Boolean getRequiresSpecialHandling() { return requiresSpecialHandling; }
    public void setRequiresSpecialHandling(Boolean requiresSpecialHandling) { this.requiresSpecialHandling = requiresSpecialHandling; }
}
