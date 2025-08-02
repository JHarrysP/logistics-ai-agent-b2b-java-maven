package com.logistics.controller;

import com.logistics.model.*;
import com.logistics.repository.ShipmentRepository;
import com.logistics.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for warehouse operations
 */
@RestController
@RequestMapping("/api/warehouse")
@Tag(name = "Warehouse", description = "Warehouse Operations API - Manage picking, loading, and shipments")
public class WarehouseController {
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Get all pending shipments awaiting pickup
     */
    @GetMapping("/pending-shipments")
    @Operation(summary = "Get Pending Shipments", 
               description = "Retrieve all shipments scheduled for pickup")
    public ResponseEntity<List<Shipment>> getPendingShipments() {
        List<Shipment> shipments = shipmentRepository.findByStatus(ShipmentStatus.SCHEDULED);
        return ResponseEntity.ok(shipments);
    }
    
    /**
     * Get shipments scheduled for today
     */
    @GetMapping("/today-shipments")
    @Operation(summary = "Get Today's Shipments", 
               description = "Retrieve all shipments scheduled for pickup today")
    public ResponseEntity<List<Shipment>> getTodayShipments() {
        List<Shipment> shipments = shipmentRepository.findShipmentsScheduledForDate(LocalDateTime.now());
        return ResponseEntity.ok(shipments);
    }
    
    /**
     * Start loading process for a shipment
     */
    @PostMapping("/shipments/{shipmentId}/start-loading")
    @Operation(summary = "Start Loading", 
               description = "Begin the loading process for a scheduled shipment")
    public ResponseEntity<String> startLoading(
            @Parameter(description = "Shipment ID", required = true) @PathVariable Long shipmentId) {
        
        Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (shipment.getStatus() != ShipmentStatus.SCHEDULED) {
            return ResponseEntity.badRequest()
                .body("Cannot start loading - shipment status is: " + shipment.getStatus());
        }
        
        shipment.setStatus(ShipmentStatus.LOADING);
        shipmentRepository.save(shipment);
        
        notificationService.sendInternalNotification("WAREHOUSE", 
            "Loading started for shipment #" + shipmentId + 
            ", Order #" + shipment.getOrder().getId() + 
            ", Truck: " + shipment.getTruckId());
        
        System.out.println("🔄 Loading started for shipment: " + shipmentId);
        
        return ResponseEntity.ok("Loading started for shipment #" + shipmentId);
    }
    
    /**
     * Complete loading process for a shipment
     */
    @PostMapping("/shipments/{shipmentId}/complete-loading")
    @Operation(summary = "Complete Loading", 
               description = "Mark the loading process as complete and ready for departure")
    public ResponseEntity<String> completeLoading(
            @Parameter(description = "Shipment ID", required = true) @PathVariable Long shipmentId) {
        
        Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (shipment.getStatus() != ShipmentStatus.LOADING) {
            return ResponseEntity.badRequest()
                .body("Cannot complete loading - shipment status is: " + shipment.getStatus());
        }
        
        shipment.setStatus(ShipmentStatus.LOADED);
        shipment.setActualPickup(LocalDateTime.now());
        shipmentRepository.save(shipment);
        
        // Update order status
        Order order = shipment.getOrder();
        order.setStatus(OrderStatus.IN_TRANSIT);
        
        notificationService.sendNotification(order.getClientId(),
            "Your order #" + order.getId() + " has been loaded and is now in transit. " +
            "Estimated delivery: " + shipment.getEstimatedDelivery() + 
            ". Truck: " + shipment.getTruckId());
        
        notificationService.sendInternalNotification("LOGISTICS", 
            "Shipment #" + shipmentId + " loaded and departed. " +
            "Truck: " + shipment.getTruckId() + 
            ", Driver: " + shipment.getDriverId());
        
        System.out.println("Loading completed for shipment: " + shipmentId);
        
        return ResponseEntity.ok("Loading completed for shipment #" + shipmentId + 
                               ". Truck is ready for departure.");
    }
    
    /**
     * Mark shipment as in transit
     */
    @PostMapping("/shipments/{shipmentId}/dispatch")
    @Operation(summary = "Dispatch Shipment", 
               description = "Mark shipment as dispatched and in transit")
    public ResponseEntity<String> dispatchShipment(
            @Parameter(description = "Shipment ID", required = true) @PathVariable Long shipmentId) {
        
        Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (shipment.getStatus() != ShipmentStatus.LOADED) {
            return ResponseEntity.badRequest()
                .body("Cannot dispatch - shipment must be loaded first. Status: " + shipment.getStatus());
        }
        
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipmentRepository.save(shipment);
        
        System.out.println("Shipment dispatched: " + shipmentId);
        
        return ResponseEntity.ok("Shipment #" + shipmentId + " dispatched and in transit");
    }
    
    /**
     * Get overdue shipments
     */
    @GetMapping("/overdue-shipments")
    @Operation(summary = "Get Overdue Shipments", 
               description = "Retrieve shipments that are overdue for pickup")
    public ResponseEntity<List<Shipment>> getOverdueShipments() {
        List<Shipment> overdueShipments = shipmentRepository.findOverdueShipments();
        return ResponseEntity.ok(overdueShipments);
    }
    
    /**
     * Get shipments requiring special handling
     */
    @GetMapping("/special-handling")
    @Operation(summary = "Get Special Handling Shipments", 
               description = "Retrieve shipments that require special handling procedures")
    public ResponseEntity<List<Shipment>> getSpecialHandlingShipments() {
        List<Shipment> specialShipments = shipmentRepository.findShipmentsRequiringSpecialHandling();
        return ResponseEntity.ok(specialShipments);
    }
    
    /**
     * Get picking instructions for a shipment
     */
    @GetMapping("/shipments/{shipmentId}/instructions")
    @Operation(summary = "Get Picking Instructions", 
               description = "Retrieve detailed picking instructions for a shipment")
    public ResponseEntity<String> getPickingInstructions(
            @Parameter(description = "Shipment ID", required = true) @PathVariable Long shipmentId) {
        
        Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) {
            return ResponseEntity.notFound().build();
        }
        
        String instructions = shipment.getPickingInstructions();
        if (instructions == null || instructions.trim().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(instructions);
    }
}