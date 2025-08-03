package com.logistics.event;

import com.logistics.model.Order;
import com.logistics.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for logistics events
 */
@Component
public class LogisticsEventListener {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Handle order received events
     */
    @EventListener
    @Async
    public void handleOrderReceived(OrderReceivedEvent event) {
        Order order = event.getOrder();
        System.out.println("📨 Event: Order received - " + order.getId());
        
        // Send acknowledgment notification
        notificationService.sendNotification(
            order.getClientId(),
            "Order #" + order.getId() + " received and is being processed by our AI agents."
        );
        
        // Internal notification
        notificationService.sendInternalNotification(
            "ORDER_PROCESSING",
            "New order received: #" + order.getId() + " from " + order.getClientName() + 
            " - " + order.getItems().size() + " items"
        );
    }
    
    /**
     * Handle order validated events
     */
    @EventListener
    @Async
    public void handleOrderValidated(OrderValidatedEvent event) {
        Order order = event.getOrder();
        System.out.println("✅ Event: Order validated - " + order.getId());
        
        // Log validation success
        notificationService.sendInternalNotification(
            "VALIDATION",
            "Order #" + order.getId() + " validated successfully - proceeding to inventory check"
        );
    }
    
    /**
     * Handle order fulfilled events
     */
    @EventListener
    @Async
    public void handleOrderFulfilled(OrderFulfilledEvent event) {
        Order order = event.getOrder();
        System.out.println("📦 Event: Order fulfilled - " + order.getId());
        
        // Notify client of fulfillment
        notificationService.sendNotification(
            order.getClientId(),
            "Order #" + order.getId() + " has been fulfilled. Inventory reserved and shipment is being scheduled."
        );
        
        // Internal notification
        notificationService.sendInternalNotification(
            "FULFILLMENT",
            "Order #" + order.getId() + " fulfilled - " + 
            "Weight: " + String.format("%.1f", order.getTotalWeight()) + "kg, " +
            "Volume: " + String.format("%.2f", order.getTotalVolume()) + "m³"
        );
        
        // Check for low stock warnings
        if (order.getTotalWeight() > 1000.0) {
            notificationService.sendInternalNotification(
                "WAREHOUSE",
                "Large order fulfilled: #" + order.getId() + " - " +
                "Weight: " + String.format("%.1f", order.getTotalWeight()) + "kg - " +
                "Review inventory levels for affected products"
            );
        }
    }
}