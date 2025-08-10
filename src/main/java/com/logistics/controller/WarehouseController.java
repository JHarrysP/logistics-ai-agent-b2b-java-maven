package com.logistics.controller;

import com.logistics.model.*;
import com.logistics.repository.ShipmentRepository;
import com.logistics.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        System.out.println(" Loading started for shipment: " + shipmentId);

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
     * Mark shipment as delivered
     */
    @PostMapping("/shipments/{shipmentId}/delivered")
    @Operation(summary = "Mark Delivered",
            description = "Mark shipment as successfully delivered")
    public ResponseEntity<String> markDelivered(
            @Parameter(description = "Shipment ID", required = true) @PathVariable Long shipmentId) {

        Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) {
            return ResponseEntity.notFound().build();
        }

        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            return ResponseEntity.badRequest()
                    .body("Cannot mark as delivered - shipment must be in transit. Status: " + shipment.getStatus());
        }

        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setActualDelivery(LocalDateTime.now());
        shipmentRepository.save(shipment);

        // Update order status
        Order order = shipment.getOrder();
        order.setStatus(OrderStatus.DELIVERED);

        notificationService.sendNotification(order.getClientId(),
                "Your order #" + order.getId() + " has been delivered successfully at " +
                        LocalDateTime.now() + ". Thank you for choosing our service!");

        notificationService.sendInternalNotification("DELIVERY",
                "Shipment #" + shipmentId + " delivered successfully. " +
                        "Order #" + order.getId() + " completed for " + order.getClientName());

        System.out.println(" Shipment delivered: " + shipmentId);

        return ResponseEntity.ok("Shipment #" + shipmentId + " marked as delivered successfully");
    }

    /**
     * Report delivery problem
     */
    @PostMapping("/shipments/{shipmentId}/delivery-problem")
    @Operation(summary = "Report Delivery Problem",
            description = "Report a problem with delivery (customer not available, address issue, etc.)")
    public ResponseEntity<String> reportDeliveryProblem(
            @Parameter(description = "Shipment ID", required = true) @PathVariable Long shipmentId,
            @Parameter(description = "Problem description") @RequestParam String problem,
            @Parameter(description = "New estimated delivery")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime newEstimatedDelivery) {

        Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) {
            return ResponseEntity.notFound().build();
        }

        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            return ResponseEntity.badRequest()
                    .body("Cannot report delivery problem - shipment status: " + shipment.getStatus());
        }

        // Update estimated delivery if provided
        if (newEstimatedDelivery != null) {
            if (newEstimatedDelivery.isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                        .body("New estimated delivery must be in the future");
            }
            shipment.setEstimatedDelivery(newEstimatedDelivery);
        }

        shipmentRepository.save(shipment);

        Order order = shipment.getOrder();

        String message = "Delivery attempt for order #" + order.getId() +
                " encountered an issue: " + problem;

        if (newEstimatedDelivery != null) {
            message += ". New estimated delivery: " + newEstimatedDelivery;
        } else {
            message += ". Our team will contact you to reschedule delivery.";
        }

        notificationService.sendNotification(order.getClientId(), message);

        notificationService.sendUrgentAlert("DELIVERY_MANAGEMENT",
                "Delivery problem reported for shipment #" + shipmentId +
                        ": " + problem + ". Client: " + order.getClientName() +
                        ", Contact required for resolution.");

        System.out.println(" Delivery problem reported: " + shipmentId + " - " + problem);

        return ResponseEntity.ok("Delivery problem reported for shipment #" + shipmentId);
    }

    /**
     * Attempt redelivery
     */
    @PostMapping("/shipments/{shipmentId}/redeliver")
    @Operation(summary = "Schedule Redelivery",
            description = "Schedule a redelivery attempt for failed delivery")
    public ResponseEntity<String> scheduleRedelivery(
            @Parameter(description = "Shipment ID", required = true) @PathVariable Long shipmentId,
            @Parameter(description = "New delivery date/time")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime newDeliveryTime,
            @Parameter(description = "Redelivery notes")
            @RequestParam(required = false) String notes) {

        Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) {
            return ResponseEntity.notFound().build();
        }

        if (newDeliveryTime.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest()
                    .body("Redelivery time must be in the future");
        }

        shipment.setEstimatedDelivery(newDeliveryTime);
        shipment.setStatus(ShipmentStatus.IN_TRANSIT); // Reset to in transit for redelivery

        // Add redelivery notes to picking instructions
        String redeliveryNote = "REDELIVERY ATTEMPT - Scheduled for: " + newDeliveryTime;
        if (notes != null && !notes.trim().isEmpty()) {
            redeliveryNote += ". Notes: " + notes;
        }

        String existingInstructions = shipment.getPickingInstructions();
        if (existingInstructions != null) {
            shipment.setPickingInstructions(existingInstructions + "\n\n" + redeliveryNote);
        } else {
            shipment.setPickingInstructions(redeliveryNote);
        }

        shipmentRepository.save(shipment);

        Order order = shipment.getOrder();

        notificationService.sendNotification(order.getClientId(),
                "Redelivery scheduled for order #" + order.getId() +
                        " on " + newDeliveryTime + ". " +
                        (notes != null ? "Special instructions: " + notes :
                                "Please ensure someone is available to receive the delivery."));

        notificationService.sendInternalNotification("DELIVERY",
                "Redelivery scheduled: Shipment #" + shipmentId +
                        ", Order #" + order.getId() +
                        ", New time: " + newDeliveryTime);

        System.out.println(" Redelivery scheduled: " + shipmentId + " for " + newDeliveryTime);

        return ResponseEntity.ok("Redelivery scheduled for shipment #" + shipmentId +
                " on " + newDeliveryTime);
    }

    /**
     * Update delivery status with driver notes
     */
    @PostMapping("/shipments/{shipmentId}/update-status")
    @Operation(summary = "Update Delivery Status",
            description = "Update delivery status with driver notes and location")
    public ResponseEntity<String> updateDeliveryStatus(
            @Parameter(description = "Shipment ID", required = true) @PathVariable Long shipmentId,
            @Parameter(description = "Status update") @RequestParam String statusUpdate,
            @Parameter(description = "Driver notes") @RequestParam(required = false) String driverNotes,
            @Parameter(description = "Current location") @RequestParam(required = false) String currentLocation) {

        Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) {
            return ResponseEntity.notFound().build();
        }

        Order order = shipment.getOrder();

        String updateMessage = "Delivery update for order #" + order.getId() + ": " + statusUpdate;

        if (currentLocation != null && !currentLocation.trim().isEmpty()) {
            updateMessage += " (Currently at: " + currentLocation + ")";
        }

        if (driverNotes != null && !driverNotes.trim().isEmpty()) {
            updateMessage += ". Driver notes: " + driverNotes;
        }

        // Send update to client
        notificationService.sendNotification(order.getClientId(), updateMessage);

        // Log internal update
        notificationService.sendInternalNotification("DELIVERY_TRACKING",
                "Status update - Shipment #" + shipmentId + ": " + statusUpdate +
                        (driverNotes != null ? " | Notes: " + driverNotes : "") +
                        (currentLocation != null ? " | Location: " + currentLocation : ""));

        System.out.println(" Delivery status update: " + shipmentId + " - " + statusUpdate);

        return ResponseEntity.ok("Delivery status updated for shipment #" + shipmentId);
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
     * Get failed deliveries
     */
    @GetMapping("/failed-deliveries")
    @Operation(summary = "Get Failed Deliveries",
            description = "Retrieve shipments with delivery issues that need attention")
    public ResponseEntity<List<Shipment>> getFailedDeliveries() {
        // This would need a custom query - for now, return shipments in transit for over 24 hours
        List<Shipment> potentialFailures = shipmentRepository.findShipmentsInTransit()
                .stream()
                .filter(s -> s.getEstimatedDelivery() != null &&
                        s.getEstimatedDelivery().isBefore(LocalDateTime.now().minusHours(24)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(potentialFailures);
    }

    /**
     * Get deliveries for today
     */
    @GetMapping("/today-deliveries")
    @Operation(summary = "Get Today's Deliveries",
            description = "Retrieve all deliveries scheduled for today")
    public ResponseEntity<List<Shipment>> getTodayDeliveries() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<Shipment> todayDeliveries = shipmentRepository.findAll()
                .stream()
                .filter(s -> s.getEstimatedDelivery() != null &&
                        s.getEstimatedDelivery().isAfter(startOfDay) &&
                        s.getEstimatedDelivery().isBefore(endOfDay))
                .collect(Collectors.toList());

        return ResponseEntity.ok(todayDeliveries);
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