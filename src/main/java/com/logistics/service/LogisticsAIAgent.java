// ============= MAIN AI AGENT ORCHESTRATOR =============
package com.logistics.service;

import com.logistics.model.*;
import com.logistics.repository.OrderRepository;
import com.logistics.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Main AI Agent orchestrating the complete logistics workflow
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
    private NotificationService notificationService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Process order asynchronously through the complete logistics workflow
     */
    @Async
    public CompletableFuture<String> processOrder(Order order) {
        try {
            System.out.println("AI Agent processing order: " + order.getId());
            
            // Step 1: Validate Order
            ValidationResult validation = orderValidationAgent.validateOrder(order);
            if (!validation.isValid()) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                notificationService.sendNotification(order.getClientId(), 
                    "Order #" + order.getId() + " cancelled: " + validation.getReason());
                return CompletableFuture.completedFuture("Order cancelled: " + validation.getReason());
            }
            
            order.setStatus(OrderStatus.VALIDATED);
            orderRepository.save(order);
            System.out.println("Order validated: " + order.getId());
            
            // Step 2: Check Inventory
            InventoryCheckResult inventoryCheck = inventoryAgent.checkInventory(order);
            if (!inventoryCheck.isAvailable()) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                notificationService.sendNotification(order.getClientId(), 
                    "Order #" + order.getId() + " cancelled: " + inventoryCheck.getMessage());
                return CompletableFuture.completedFuture("Order cancelled: " + inventoryCheck.getMessage());
            }
            
            order.setStatus(OrderStatus.INVENTORY_CHECKED);
            orderRepository.save(order);
            System.out.println("Inventory checked: " + order.getId());
            
            // Step 3: Fulfill Order (Reserve inventory)
            FulfillmentResult fulfillment = fulfillmentAgent.fulfillOrder(order);
            if (!fulfillment.isSuccessful()) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                return CompletableFuture.completedFuture("Order fulfillment failed: " + fulfillment.getMessage());
            }
            
            order.setStatus(OrderStatus.FULFILLED);
            orderRepository.save(order);
            System.out.println("Order fulfilled: " + order.getId());
            
            // Step 4: Generate Warehouse Instructions
            WarehouseInstructions instructions = warehouseAgent.generatePickingInstructions(order);
            System.out.println("Warehouse instructions generated: " + order.getId());
            
            // Step 5: Schedule Shipment
            Shipment shipment = shippingAgent.scheduleShipment(order, instructions);
            order.setStatus(OrderStatus.READY_FOR_PICKUP);
            orderRepository.save(order);
            
            notificationService.sendNotification(order.getClientId(), 
                "Order #" + order.getId() + " processed successfully. " +
                "Shipment #" + shipment.getId() + " scheduled for pickup at " + 
                shipment.getScheduledPickup() + ". Truck: " + shipment.getTruckId());
            
            System.out.println("Shipment scheduled: " + shipment.getId() + " for order: " + order.getId());
            
            return CompletableFuture.completedFuture(
                "Order processed successfully. Shipment ID: " + shipment.getId());
            
        } catch (Exception e) {
            System.err.println("Error processing order " + order.getId() + ": " + e.getMessage());
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            notificationService.sendNotification(order.getClientId(), 
                "Order #" + order.getId() + " processing failed: " + e.getMessage());
            return CompletableFuture.completedFuture("Order processing failed: " + e.getMessage());
        }
    }
}
