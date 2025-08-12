package com.logistics.controller;

import com.logistics.model.*;
import com.logistics.repository.OrderRepository;
import com.logistics.repository.ShipmentRepository;
import com.logistics.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced REST Controller for warehouse operations with proper status propagation
 */
@RestController
@RequestMapping("/api/warehouse")
@Tag(name = "Warehouse", description = "Warehouse Operations API - Manage picking, loading, and shipments")
public class WarehouseController {


    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Get all pending shipments awaiting pickup
     */
    @GetMapping("/pending-shipments")
    @Operation(summary = "Get Pending Shipments",
            description = "Retrieve all shipments scheduled for pickup")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Shipment>> getPendingShipments() {
        try {
            List<Shipment> shipments = shipmentRepository.findByStatus(ShipmentStatus.SCHEDULED);
            System.out.println(" Found " + shipments.size() + " pending shipments");
            return ResponseEntity.ok(shipments);
        } catch (Exception e) {
            System.err.println("Error getting pending shipments: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ArrayList<>());
        }
    }


    /**
     * Get shipments scheduled for today - FIXED
     */
    @GetMapping("/today-shipments")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Shipment>> getTodayShipments() {
        List<Shipment> shipments = shipmentRepository.findShipmentsScheduledForToday();
        return ResponseEntity.ok(shipments);
    }

    /**
     * Start loading process for a shipment
     */
    @PostMapping("/shipments/{shipmentId}/start-loading")
    @Operation(summary = "Start Loading",
            description = "Begin the loading process for a scheduled shipment")
    @Transactional
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

        // Update shipment status
        shipment.setStatus(ShipmentStatus.LOADING);
        shipmentRepository.save(shipment);

                // Update order status to LOADING
        Order order = shipment.getOrder();
        if (order != null && order.getStatus() == OrderStatus.READY_FOR_PICKUP) {
            order.setStatus(OrderStatus.LOADING);
            orderRepository.save(order);
            System.out.println("Order status updated to LOADING: " + order.getId());
        }

        notificationService.sendInternalNotification("WAREHOUSE",
                "Loading started for shipment #" + shipmentId +
                        ", Order #" + shipment.getOrder().getId() +
                        ", Truck: " + shipment.getTruckId());

        System.out.println("Loading started for shipment: " + shipmentId);

        return ResponseEntity.ok("Loading started for shipment #" + shipmentId);
    }

    /**
     * Complete loading process for a shipment
     */
    @PostMapping("/shipments/{shipmentId}/complete-loading")
    @Operation(summary = "Complete Loading",
            description = "Mark the loading process as complete and ready for departure")
    @Transactional
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

        // Update shipment status and times
        shipment.setStatus(ShipmentStatus.LOADED);
        shipment.setActualPickup(LocalDateTime.now());
        shipmentRepository.save(shipment);

        // Update order status
        Order order = shipment.getOrder();
        if (order != null && order.getStatus() == OrderStatus.LOADING) {
            order.setStatus(OrderStatus.FULFILLED);
            orderRepository.save(order);
            System.out.println(" Order status updated to FULFILLED: " + order.getId());
        }

        notificationService.sendNotification(order.getClientId(),
                "Your order #" + order.getId() + " has been loaded and is ready for dispatch. " +
                        "Estimated delivery: " + shipment.getEstimatedDelivery() +
                        ". Truck: " + shipment.getTruckId());

        notificationService.sendInternalNotification("LOGISTICS",
                "Shipment #" + shipmentId + " loaded and ready for dispatch. " +
                        "Truck: " + shipment.getTruckId() +
                        ", Driver: " + shipment.getDriverId());

        System.out.println(" Loading completed for shipment: " + shipmentId);

        return ResponseEntity.ok("Loading completed for shipment #" + shipmentId +
                ". Truck is ready for departure.");
    }

    /**
     * Mark shipment as in transit
     */
    @PostMapping("/shipments/{shipmentId}/dispatch")
    @Operation(summary = "Dispatch Shipment",
            description = "Mark shipment as dispatched and in transit")
    @Transactional
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

        // Update shipment status
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipmentRepository.save(shipment);

        // FIXED: Update order status and save explicitly
        Order order = shipment.getOrder();
        if (order.getStatus() == OrderStatus.FULFILLED) {
            order.setStatus(OrderStatus.IN_TRANSIT);
            orderRepository.save(order);
        }

        notificationService.sendNotification(order.getClientId(),
                "Your order #" + order.getId() + " is now in transit. " +
                        "Truck: " + shipment.getTruckId() +
                        ". Estimated delivery: " + shipment.getEstimatedDelivery());

        System.out.println("Shipment dispatched: " + shipmentId);

        return ResponseEntity.ok("Shipment #" + shipmentId + " dispatched and in transit");
    }

    /**
     * Mark shipment as delivered
     */
    @PostMapping("/shipments/{shipmentId}/delivered")
    @Operation(summary = "Mark Delivered",
            description = "Mark shipment as successfully delivered")
    @Transactional
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

        // Update shipment status and delivery time
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setActualDelivery(LocalDateTime.now());
        shipmentRepository.save(shipment);

        // FIXED: Update order status and save explicitly
        Order order = shipment.getOrder();
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        notificationService.sendNotification(order.getClientId(),
                "Your order #" + order.getId() + " has been delivered successfully at " +
                        LocalDateTime.now() + ". Thank you for choosing our service!");

        notificationService.sendInternalNotification("DELIVERY",
                "Shipment #" + shipmentId + " delivered successfully. " +
                        "Order #" + order.getId() + " completed for " + order.getClientName());

        System.out.println("Shipment delivered: " + shipmentId);

        return ResponseEntity.ok("Shipment #" + shipmentId + " marked as delivered successfully");
    }

    /**
     * Report delivery problem
     */
    @PostMapping("/shipments/{shipmentId}/delivery-problem")
    @Operation(summary = "Report Delivery Problem",
            description = "Report a problem with delivery (customer not available, address issue, etc.)")
    @Transactional
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

        System.out.println("Delivery problem reported: " + shipmentId + " - " + problem);

        return ResponseEntity.ok("Delivery problem reported for shipment #" + shipmentId);
    }

    // ... (keeping remaining methods unchanged for brevity)

    /**
     * Get overdue shipments
     */
    @GetMapping("/overdue-shipments")
    @Operation(summary = "Get Overdue Shipments",
            description = "Retrieve shipments that are overdue for pickup")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Shipment>> getOverdueShipments() {
        try {
            List<Shipment> overdueShipments = shipmentRepository.findOverdueShipments();
            System.out.println(" Found " + overdueShipments.size() + " overdue shipments");
            return ResponseEntity.ok(overdueShipments);
        } catch (Exception e) {
            System.err.println("Error getting overdue shipments: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ArrayList<>());
        }
    }

    /**
     * Get shipments requiring special handling
     */
    @GetMapping("/special-handling")
    @Operation(summary = "Get Special Handling Shipments",
            description = "Retrieve shipments that require special handling procedures")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Shipment>> getSpecialHandlingShipments() {
        List<Shipment> specialShipments = shipmentRepository.findShipmentsRequiringSpecialHandling();
        return ResponseEntity.ok(specialShipments);
    }
    // Add these methods to your WarehouseController.java

    /**
     * Mark order as delayed with a new delivery date
     */
    @PostMapping("/orders/{orderId}/delay")
    @Operation(summary = "Mark Order as Delayed",
            description = "Mark an order as delayed with a new estimated delivery date")
    @Transactional
    public ResponseEntity<String> markOrderDelayed(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId,
            @Parameter(description = "New estimated delivery date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newEstimatedDelivery,
            @Parameter(description = "Reason for delay") @RequestParam(required = false) String reason) {

        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }

            // Update delivery date if order has a shipment
            List<Shipment> shipments = shipmentRepository.findByOrderId(orderId);
            if (!shipments.isEmpty()) {
                Shipment shipment = shipments.get(0);
                shipment.setEstimatedDelivery(newEstimatedDelivery);
                shipmentRepository.save(shipment);
            }

            String message = "Order #" + orderId + " marked as delayed";
            if (reason != null && !reason.trim().isEmpty()) {
                message += ". Reason: " + reason;
            }
            message += ". New estimated delivery: " + newEstimatedDelivery;

            notificationService.sendNotification(order.getClientId(), message);

            System.out.println("Order delayed: " + orderId + " - " + reason);

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            System.err.println("Error marking order as delayed " + orderId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error marking order as delayed: " + e.getMessage());
        }
    }

    /**
     * Reject/Cancel an order with reason
     */
    @PostMapping("/orders/{orderId}/reject")
    @Operation(summary = "Reject Order",
            description = "Reject/cancel an order with a specified reason")
    @Transactional
    public ResponseEntity<String> rejectOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId,
            @Parameter(description = "Rejection reason") @RequestParam(required = false) String reason) {

        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if order can be rejected
            if (order.getStatus() == OrderStatus.DELIVERED) {
                return ResponseEntity.badRequest()
                        .body("Cannot reject order - already delivered");
            }

            OrderStatus oldStatus = order.getStatus();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            String message = "Order #" + orderId + " has been rejected/cancelled";
            if (reason != null && !reason.trim().isEmpty()) {
                message += ". Reason: " + reason;
            }

            notificationService.sendNotification(order.getClientId(), message);

            System.out.println("Order rejected: " + orderId + " - " + reason);

            return ResponseEntity.ok("Order #" + orderId + " rejected successfully");

        } catch (Exception e) {
            System.err.println("Error rejecting order " + orderId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error rejecting order: " + e.getMessage());
        }
    }
    /**
     * Get failed deliveries
     */
    @GetMapping("/failed-deliveries")
    @Operation(summary = "Get Failed Deliveries",
            description = "Retrieve shipments with delivery issues that need attention")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Shipment>> getFailedDeliveries() {
        List<Shipment> potentialFailures = shipmentRepository.findShipmentsInTransit()
                .stream()
                .filter(s -> s.getEstimatedDelivery() != null &&
                        s.getEstimatedDelivery().isBefore(LocalDateTime.now().minusHours(24)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(potentialFailures);
    }
}
