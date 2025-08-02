// ============= SHIPMENT ENTITY =============
package com.logistics.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Shipment entity representing the delivery logistics for an order
 */
@Entity
@Table(name = "shipments")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(nullable = false, length = 50)
    private String truckId;
    
    @Column(nullable = false, length = 50)
    private String driverId;
    
    @Column(nullable = false)
    private LocalDateTime scheduledPickup;
    
    private LocalDateTime actualPickup;
    
    @Column(nullable = false)
    private LocalDateTime estimatedDelivery;
    
    private LocalDateTime actualDelivery;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;
    
    @Column(length = 1000)
    private String pickingInstructions;
    
    @Column(nullable = false)
    private Boolean requiresSpecialHandling = false;
    
    // Constructors
    public Shipment() {}
    
    public Shipment(Order order, String truckId, String driverId, LocalDateTime scheduledPickup) {
        this.order = order;
        this.truckId = truckId;
        this.driverId = driverId;
        this.scheduledPickup = scheduledPickup;
        this.status = ShipmentStatus.SCHEDULED;
        this.requiresSpecialHandling = false;
    }
    
    // Business methods
    public boolean isOverdue() {
        return scheduledPickup.isBefore(LocalDateTime.now()) && actualPickup == null;
    }
    
    public long getEstimatedDeliveryHours() {
        return java.time.Duration.between(scheduledPickup, estimatedDelivery).toHours();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public String getTruckId() { return truckId; }
    public void setTruckId(String truckId) { this.truckId = truckId; }
    
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    
    public LocalDateTime getScheduledPickup() { return scheduledPickup; }
    public void setScheduledPickup(LocalDateTime scheduledPickup) { this.scheduledPickup = scheduledPickup; }
    
    public LocalDateTime getActualPickup() { return actualPickup; }
    public void setActualPickup(LocalDateTime actualPickup) { this.actualPickup = actualPickup; }
    
    public LocalDateTime getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    
    public LocalDateTime getActualDelivery() { return actualDelivery; }
    public void setActualDelivery(LocalDateTime actualDelivery) { this.actualDelivery = actualDelivery; }
    
    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    
    public String getPickingInstructions() { return pickingInstructions; }
    public void setPickingInstructions(String pickingInstructions) { this.pickingInstructions = pickingInstructions; }
    
    public Boolean getRequiresSpecialHandling() { return requiresSpecialHandling; }
    public void setRequiresSpecialHandling(Boolean requiresSpecialHandling) { this.requiresSpecialHandling = requiresSpecialHandling; }
}