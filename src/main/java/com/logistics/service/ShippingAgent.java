// ============= SHIPPING AGENT =============
package com.logistics.service;

import com.logistics.model.Order;
import com.logistics.model.Shipment;
import com.logistics.repository.ShipmentRepository;
import com.logistics.util.WarehouseInstructions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AI Agent specialized in shipping and logistics coordination
 */
@Service
public class ShippingAgent {
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    /**
     * Schedule shipment using AI-driven optimization
     */
    public Shipment scheduleShipment(Order order, WarehouseInstructions instructions) {
        System.out.println("Scheduling shipment for order: " + order.getId());
        
        // AI-driven truck selection
        String truckId = selectOptimalTruck(order);
        System.out.println("Selected truck: " + truckId);
        
        // AI-driven driver assignment
        String driverId = assignOptimalDriver(order, truckId);
        System.out.println("Assigned driver: " + driverId);
        
        // AI-calculated optimal pickup time
        LocalDateTime scheduledPickup = calculateOptimalPickupTime(order, instructions);
        System.out.println("Scheduled pickup: " + scheduledPickup);
        
        // Create shipment
        Shipment shipment = new Shipment(order, truckId, driverId, scheduledPickup);
        shipment.setPickingInstructions(instructions.getInstructions());
        shipment.setRequiresSpecialHandling(instructions.requiresSpecialHandling());
        
        // AI-estimated delivery time
        LocalDateTime estimatedDelivery = calculateEstimatedDelivery(scheduledPickup, order);
        shipment.setEstimatedDelivery(estimatedDelivery);
        
        System.out.println("Estimated delivery: " + estimatedDelivery);
        
        return shipmentRepository.save(shipment);
    }
    
    /**
     * AI algorithm for optimal truck selection
     */
    private String selectOptimalTruck(Order order) {
        double weight = order.getTotalWeight();
        double volume = order.getTotalVolume();
        
        // AI decision tree for truck selection
        if (weight > 2000.0 || volume > 25.0) {
            return "TRUCK_LARGE_001"; // Large truck for heavy/bulky orders
        } else if (weight > 800.0 || volume > 15.0) {
            return "TRUCK_MEDIUM_002"; // Medium truck for standard orders
        } else if (hasFragileItems(order)) {
            return "TRUCK_FRAGILE_003"; // Specialized truck for fragile items
        } else {
            return "TRUCK_SMALL_004"; // Small truck for light orders
        }
    }
    
    /**
     * AI algorithm for optimal driver assignment
     */
    private String assignOptimalDriver(Order order, String truckId) {
        // AI matching based on order characteristics and driver expertise
        if (hasFragileItems(order)) {
            return "DRIVER_FRAGILE_SPECIALIST_001"; // Driver experienced with fragile items
        } else if (order.getTotalWeight() > 1500.0) {
            return "DRIVER_HEAVY_LOADS_002"; // Driver experienced with heavy loads
        } else if (isUrgentDelivery(order)) {
            return "DRIVER_EXPRESS_003"; // Fast, reliable driver for urgent deliveries
        } else {
            return "DRIVER_GENERAL_004"; // General purpose driver
        }
    }
    
    /**
     * AI algorithm to calculate optimal pickup time
     */
    private LocalDateTime calculateOptimalPickupTime(Order order, WarehouseInstructions instructions) {
        LocalDateTime now = LocalDateTime.now();
        
        // Calculate minimum preparation time
        int preparationHours = 2; // Base preparation time
        preparationHours += instructions.getEstimatedPickingTime() / 60; // Add picking time
        
        if (instructions.requiresSpecialHandling()) {
            preparationHours += 1; // Extra time for special handling
        }
        
        LocalDateTime earliestPickup = now.plusHours(preparationHours);
        
        // Align with business hours (8 AM - 6 PM)
        if (earliestPickup.getHour() < 8) {
            earliestPickup = earliestPickup.withHour(8).withMinute(0);
        } else if (earliestPickup.getHour() >= 18) {
            earliestPickup = earliestPickup.plusDays(1).withHour(8).withMinute(0);
        }
        
        // Consider requested delivery date
        LocalDateTime requestedDelivery = order.getRequestedDeliveryDate();
        long deliveryHours = calculateDeliveryDuration(order);
        LocalDateTime latestPickup = requestedDelivery.minusHours(deliveryHours);
        
        // Return the later of the two times (ensuring both constraints are met)
        return earliestPickup.isAfter(latestPickup) ? earliestPickup : latestPickup;
    }
    
    /**
     * AI algorithm to calculate estimated delivery time
     */
    private LocalDateTime calculateEstimatedDelivery(LocalDateTime pickupTime, Order order) {
        long deliveryHours = calculateDeliveryDuration(order);
        return pickupTime.plusHours(deliveryHours);
    }
    
    /**
     * AI algorithm to calculate delivery duration
     */
    private long calculateDeliveryDuration(Order order) {
        // Base delivery time
        long baseHours = 4;
        
        // Add time based on order characteristics
        if (order.getTotalWeight() > 1000.0) {
            baseHours += 1; // Heavy loads take longer
        }
        
        if (hasFragileItems(order)) {
            baseHours += 1; // Fragile items require careful driving
        }
        
        // Add time based on destination (simple heuristic)
        String address = order.getDeliveryAddress().toLowerCase();
        if (address.contains("hamburg")) {
            baseHours += 2; // Local Hamburg delivery
        } else if (address.contains("berlin") || address.contains("munich")) {
            baseHours += 6; // Major cities farther away
        } else {
            baseHours += 4; // Default for other German cities
        }
        
        return baseHours;
    }
    
    /**
     * Helper method to check if order contains fragile items
     */
    private boolean hasFragileItems(Order order) {
        return order.getItems().stream()
            .anyMatch(item -> item.getProduct().isFragile());
    }
    
    /**
     * Helper method to check if delivery is urgent (within 2 days)
     */
    private boolean isUrgentDelivery(Order order) {
        return order.getRequestedDeliveryDate().isBefore(LocalDateTime.now().plusDays(2));
    }
}
