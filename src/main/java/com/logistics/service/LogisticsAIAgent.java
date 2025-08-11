package com.logistics.service;

import com.logistics.model.*;
import com.logistics.repository.OrderRepository;
import com.logistics.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * Enhanced Main AI Agent orchestrating the complete logistics workflow with real-time updates
 */
@Service
public class LogisticsAIAgent {

    @Autowired
    private OrderValidationAgent orderValidationAgent;

    @Autowired
    private InventoryAgent inventoryAgent;

    @Autowired
    private FulfillmentAgent fulfillmentAgent;

    @Autowired
    private WarehouseAgent warehouseAgent;

    @Autowired
    private ShippingAgent shippingAgent;

    @Autowired
    private RealtimeNotificationService notificationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Process order asynchronously through the complete logistics workflow with real-time updates
     */
    @Async("aiAgentExecutor")
    @Transactional
    public CompletableFuture<String> processOrder(Order order) {
        try {
            System.out.println(" AI Agent processing order: " + order.getId());

            // Send AI processing notification
            notificationService.sendAIAlert("LogisticsAIAgent", "Started processing order #" + order.getId(),
                    java.util.Map.of("orderId", order.getId(), "stage", "INITIATED"));

            // Step 1: Validate Order
            ValidationResult validation = orderValidationAgent.validateOrder(order);
            if (!validation.isValid()) {
                OrderStatus oldStatus = order.getStatus();
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                notificationService.sendOrderStatusUpdate(order.getId(), oldStatus.toString(), "CANCELLED");
                notificationService.sendNotification(order.getClientId(),
                        "Order #" + order.getId() + " cancelled: " + validation.getReason());

                return CompletableFuture.completedFuture("Order cancelled: " + validation.getReason());
            }

            // Update status with real-time notification
            OrderStatus oldStatus = order.getStatus();
            order.setStatus(OrderStatus.VALIDATED);
            orderRepository.save(order);
            notificationService.sendOrderStatusUpdate(order.getId(), oldStatus.toString(), "VALIDATED");
            notificationService.sendAIAlert("ValidationAgent", "Order validated successfully",
                    java.util.Map.of("orderId", order.getId()));
            System.out.println(" Order validated: " + order.getId());

            // Step 2: Check Inventory
            InventoryCheckResult inventoryCheck = inventoryAgent.checkInventory(order);
            if (!inventoryCheck.isAvailable()) {
                oldStatus = order.getStatus();
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                notificationService.sendOrderStatusUpdate(order.getId(), oldStatus.toString(), "CANCELLED");
                notificationService.sendNotification(order.getClientId(),
                        "Order #" + order.getId() + " cancelled: " + inventoryCheck.getMessage());

                return CompletableFuture.completedFuture("Order cancelled: " + inventoryCheck.getMessage());
            }

            oldStatus = order.getStatus();
            order.setStatus(OrderStatus.INVENTORY_CHECKED);
            orderRepository.save(order);
            notificationService.sendOrderStatusUpdate(order.getId(), oldStatus.toString(), "INVENTORY_CHECKED");
            notificationService.sendAIAlert("InventoryAgent", "Inventory check passed",
                    java.util.Map.of("orderId", order.getId()));
            System.out.println(" Inventory checked: " + order.getId());

            // Step 3: Fulfill Order (Reserve inventory)
            FulfillmentResult fulfillment = fulfillmentAgent.fulfillOrder(order);
            if (!fulfillment.isSuccessful()) {
                oldStatus = order.getStatus();
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                notificationService.sendOrderStatusUpdate(order.getId(), oldStatus.toString(), "CANCELLED");
                return CompletableFuture.completedFuture("Order fulfillment failed: " + fulfillment.getMessage());
            }

            oldStatus = order.getStatus();
            order.setStatus(OrderStatus.FULFILLED);
            orderRepository.save(order);
            notificationService.sendOrderStatusUpdate(order.getId(), oldStatus.toString(), "FULFILLED");
            notificationService.sendAIAlert("FulfillmentAgent", "Order fulfilled - inventory reserved",
                    java.util.Map.of("orderId", order.getId(), "weight", order.getTotalWeight()));
            System.out.println(" Order fulfilled: " + order.getId());

            // Step 4: Generate Warehouse Instructions
            WarehouseInstructions instructions = warehouseAgent.generatePickingInstructions(order);
            notificationService.sendAIAlert("WarehouseAgent", "Picking instructions generated",
                    java.util.Map.of("orderId", order.getId(), "specialHandling", instructions.requiresSpecialHandling()));
            System.out.println(" Warehouse instructions generated: " + order.getId());

            // Step 5: Schedule Shipment
            Shipment shipment = shippingAgent.scheduleShipment(order, instructions);
            oldStatus = order.getStatus();
            order.setStatus(OrderStatus.READY_FOR_PICKUP);
            orderRepository.save(order);

            notificationService.sendOrderStatusUpdate(order.getId(), oldStatus.toString(), "READY_FOR_PICKUP");
            notificationService.sendAIAlert("ShippingAgent", "Shipment scheduled",
                    java.util.Map.of("orderId", order.getId(), "shipmentId", shipment.getId(),
                            "truckId", shipment.getTruckId(), "estimatedDelivery", shipment.getEstimatedDelivery()));

            notificationService.sendNotification(order.getClientId(),
                    "Order #" + order.getId() + " processed successfully by our AI system. " +
                            "Shipment #" + shipment.getId() + " scheduled for pickup at " +
                            shipment.getScheduledPickup() + ". Truck: " + shipment.getTruckId() +
                            ". Estimated delivery: " + shipment.getEstimatedDelivery());

            System.out.println(" Shipment scheduled: " + shipment.getId() + " for order: " + order.getId());

            // Final AI completion notification
            notificationService.sendAIAlert("LogisticsAIAgent", "Order processing completed successfully",
                    java.util.Map.of("orderId", order.getId(), "shipmentId", shipment.getId(),
                            "processingTime", "AI automated", "status", "SUCCESS"));

            return CompletableFuture.completedFuture(
                    "Order processed successfully by AI. Shipment ID: " + shipment.getId());

        } catch (Exception e) {
            System.err.println(" Error processing order " + order.getId() + ": " + e.getMessage());
            e.printStackTrace();

            // Update order status and send notifications
            OrderStatus oldStatus = order.getStatus();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            notificationService.sendOrderStatusUpdate(order.getId(), oldStatus.toString(), "CANCELLED");
            notificationService.sendNotification(order.getClientId(),
                    "Order #" + order.getId() + " processing failed: " + e.getMessage());

            notificationService.sendAIAlert("LogisticsAIAgent", "Order processing failed",
                    java.util.Map.of("orderId", order.getId(), "error", e.getMessage(), "status", "ERROR"));

            return CompletableFuture.completedFuture("Order processing failed: " + e.getMessage());
        }
    }

    /**
     * Process status update with real-time notifications
     */
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus, String reason) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                System.err.println(" Order not found for status update: " + orderId);
                return;
            }

            OrderStatus oldStatus = order.getStatus();
            order.setStatus(newStatus);
            orderRepository.save(order);

            // Send real-time status update
            notificationService.sendOrderStatusUpdate(orderId, oldStatus.toString(), newStatus.toString());

            // Send AI alert for status change
            notificationService.sendAIAlert("StatusManager", "Status updated: " + oldStatus + " → " + newStatus,
                    java.util.Map.of("orderId", orderId, "reason", reason != null ? reason : "Manual update"));

            System.out.println(" Order status updated: " + orderId + " - " + oldStatus + " → " + newStatus);

        } catch (Exception e) {
            System.err.println(" Error updating order status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get processing metrics for AI performance monitoring
     */
    public java.util.Map<String, Object> getAIMetrics() {
        // In a real system, these would be tracked metrics
        return java.util.Map.of(
                "totalOrdersProcessed", orderRepository.count(),
                "automationSuccessRate", 95.8,
                "averageProcessingTime", "4.2 minutes",
                "costSavings", "€15,240 this month",
                "uptime", "99.7%"
        );
    }
}