package com.logistics.service;

import com.logistics.model.*;
import com.logistics.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Random;

/**
 * AI-driven automation service for autonomous logistics operations
 */
@Service
public class AIAutomationService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NotificationService notificationService;

    private final Random random = new Random();

    /**
     * AI Agent: Autonomous Order Monitoring
     * Runs every 2 minutes to check for stuck orders
     */
    private static final Logger log = LoggerFactory.getLogger(AIAutomationService.class);

    @Scheduled(fixedRate = 120000) // Every 2 minutes
    @Transactional
    public void autonomousOrderMonitoring() {
        log.info("AI Agent: Starting autonomous order monitoring cycle");

        try {
            List<Order> stuckOrders = findStuckOrders();

            if (stuckOrders.isEmpty()) {
                log.debug("No stuck orders found during monitoring cycle");
                return;
            }

            log.info("Found {} stuck orders requiring attention", stuckOrders.size());

            for (Order order : stuckOrders) {
                try {
                    if (order == null || order.getId() == null) {
                        log.warn("Skipping null or invalid order during stuck order processing");
                        continue;
                    }

                    log.warn("AI detected stuck order: {} in status {} - processing time exceeded threshold", 
                            order.getId(), order.getStatus());

                    if (shouldAutoAdvanceOrder(order)) {
                        autoAdvanceOrderStatus(order);
                        log.info("Successfully auto-advanced stuck order: {}", order.getId());
                    } else {
                        escalateStuckOrder(order);
                        log.warn("Escalated stuck order {} for manual intervention", order.getId());
                    }
                } catch (Exception orderEx) {
                    log.error("Error processing stuck order {}: {}", 
                        (order != null ? order.getId() : "unknown"), orderEx.getMessage(), orderEx);
                }
            }

            checkOverdueDeliveries();
            log.info("Completed autonomous order monitoring cycle");

        } catch (Exception e) {
            log.error("Critical error in autonomous order monitoring: {}", e.getMessage(), e);
        }
    }

    /**
     * AI Agent: Predictive Delivery Time Adjustment
     * Runs every 5 minutes to update delivery estimates based on real-time factors
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void predictiveDeliveryAdjustment() {
        System.out.println("AI Agent: Running predictive delivery time adjustment...");

        try {
            List<Shipment> activeShipments = shipmentRepository.findShipmentsInTransit();

            for (Shipment shipment : activeShipments) {
                try {
                    // Add null check and error handling
                    if (shipment == null || shipment.getOrder() == null) {
                        System.err.println("Skipping shipment with null order data");
                        continue;
                    }

                    LocalDateTime newEstimate = calculatePredictiveDeliveryTime(shipment);

                    // If significant change detected, update and notify
                    if (isSignificantTimeChange(shipment.getEstimatedDelivery(), newEstimate)) {
                        shipment.setEstimatedDelivery(newEstimate);
                        shipmentRepository.save(shipment);

                        // Safe access to order properties with error handling
                        String clientId = null;
                        Long orderId = null;
                        try {
                            clientId = shipment.getOrder().getClientId();
                            orderId = shipment.getOrder().getId();
                        } catch (Exception e) {
                            System.err.println("Error accessing order data for shipment " + shipment.getId() + ": " + e.getMessage());
                            continue;
                        }

                        if (clientId != null && orderId != null) {
                            notificationService.sendNotification(
                                    clientId,
                                    "AI Update: Delivery time for order #" + orderId +
                                            " updated to " + newEstimate + " based on real-time conditions."
                            );
                        }

                        System.out.println("AI updated delivery estimate for shipment " + shipment.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Error processing shipment " + shipment.getId() + ": " + e.getMessage());
                    // Continue with next shipment
                }
            }
        } catch (Exception e) {
            System.err.println("Error in predictive delivery adjustment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * AI Agent: Intelligent Inventory Reordering
     * Runs every hour to predict and trigger inventory restocking
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void intelligentInventoryReordering() {
        System.out.println("AI Agent: Running intelligent inventory analysis...");

        List<Product> lowStockProducts = productRepository.findByStockQuantityLessThan(50);

        for (Product product : lowStockProducts) {
            int predictedDemand = calculatePredictedDemand(product);
            int reorderQuantity = calculateOptimalReorderQuantity(product, predictedDemand);

            if (reorderQuantity > 0) {
                // Simulate reorder (in a real system, would integrate with suppliers)
                product.setStockQuantity(product.getStockQuantity() + reorderQuantity);
                productRepository.save(product);

                notificationService.sendInternalNotification("INVENTORY",
                        "AI Auto-reorder: " + reorderQuantity + " units of " + product.getName() +
                                " (SKU: " + product.getSku() + ") - Predicted demand: " + predictedDemand);

                System.out.println("AI triggered reorder: " + product.getSku() + " +" + reorderQuantity);
            }
        }
    }

    /**
     * AI Agent: Anomaly Detection
     * Runs every 10 minutes to detect unusual patterns
     */
    @Scheduled(fixedRate = 600000) // Every 10 minutes
    @Transactional
    public void anomalyDetection() {
        System.out.println("AI Agent: Running anomaly detection...");

        // Detect unusually long processing times
        List<Order> suspiciousOrders = orderRepository.findAll().stream()
                .filter(order -> {
                    long hoursInStatus = java.time.Duration.between(order.getOrderDate(), LocalDateTime.now()).toHours();
                    return hoursInStatus > getExpectedProcessingTime(order.getStatus()) * 2; // 2x normal time
                })
                .collect(Collectors.toList());

        if (!suspiciousOrders.isEmpty()) {
            notificationService.sendUrgentAlert("OPERATIONS",
                    "AI Anomaly Alert: " + suspiciousOrders.size() + " orders detected with unusual processing times. " +
                            "Order IDs: " + suspiciousOrders.stream().map(o -> o.getId().toString()).collect(Collectors.joining(", ")));
        }

        // Detect unusual product demand patterns
        detectDemandAnomalies();
    }

    /**
     * AI Agent: Dynamic Route Optimization
     * Runs every 15 minutes to optimize delivery routes
     */
    @Scheduled(fixedRate = 900000) // Every 15 minutes
    @Transactional
    public void dynamicRouteOptimization() {
        System.out.println("AI Agent: Running dynamic route optimization...");

        // Group shipments by truck and optimize routes
        Map<String, List<Shipment>> shipmentsByTruck = shipmentRepository.findShipmentsInTransit()
                .stream()
                .collect(Collectors.groupingBy(Shipment::getTruckId));

        for (Map.Entry<String, List<Shipment>> entry : shipmentsByTruck.entrySet()) {
            String truckId = entry.getKey();
            List<Shipment> truckShipments = entry.getValue();

            if (truckShipments.size() > 1) {
                // AI route optimization logic
                optimizeDeliveryRoute(truckId, truckShipments);
            }
        }
    }

    // ==================== AI ALGORITHMS ====================

    private List<Order> findStuckOrders() {
        return orderRepository.findAll().stream()
                .filter(order -> {
                    long hoursInStatus = java.time.Duration.between(order.getOrderDate(), LocalDateTime.now()).toHours();
                    return hoursInStatus > getExpectedProcessingTime(order.getStatus());
                })
                .collect(Collectors.toList());
    }

    private boolean shouldAutoAdvanceOrder(Order order) {
        // AI decision logic based on order characteristics
        switch (order.getStatus()) {
            case RECEIVED:
                // Auto-advance if order is simple (small, standard products)
                return order.getTotalWeight() < 100 && order.getItems().size() <= 3;
            case SCHEDULED:
                // Auto-advance scheduled orders after reasonable time
                return true; // Most scheduled orders can be advanced to validation
            case VALIDATED:
                // Auto-advance if inventory is clearly available
                return order.getItems().stream().allMatch(item ->
                        item.getProduct().getStockQuantity() > item.getQuantity() * 2);
            case FULFILLED:
                // Auto-advance if no special handling required
                return order.getItems().stream().noneMatch(item ->
                        item.getProduct().isFragile() || item.getProduct().isHeavy());
            default:
                return false;
        }
    }


    private void autoAdvanceOrderStatus(Order order) {
        OrderStatus newStatus = getNextStatus(order.getStatus());
        if (newStatus != null) {
            order.setStatus(newStatus);
            orderRepository.save(order);

            notificationService.sendInternalNotification("AI_AUTOMATION",
                    "AI auto-advanced order #" + order.getId() + " from " +
                            order.getStatus() + " to " + newStatus);

            System.out.println("AI auto-advanced order " + order.getId() + " to " + newStatus);
        }
    }

    private OrderStatus getNextStatus(OrderStatus current) {
        switch (current) {
            case RECEIVED: return OrderStatus.SCHEDULED;
            case SCHEDULED: return OrderStatus.VALIDATED;  // Add this case
            case VALIDATED: return OrderStatus.INVENTORY_CHECKED;
            case INVENTORY_CHECKED: return OrderStatus.FULFILLED;
            case FULFILLED: return OrderStatus.READY_FOR_PICKUP;
            default: return null;
        }
    }

    private void escalateStuckOrder(Order order) {
        notificationService.sendUrgentAlert("ORDER_MANAGEMENT",
                "Order #" + order.getId() + " has been stuck in " + order.getStatus() +
                        " status for excessive time. Manual intervention required.");
    }

    private void checkOverdueDeliveries() {
        List<Shipment> overdueShipments = shipmentRepository.findAll().stream()
                .filter(s -> s.getEstimatedDelivery() != null &&
                        s.getEstimatedDelivery().isBefore(LocalDateTime.now()) &&
                        s.getStatus() == ShipmentStatus.IN_TRANSIT)
                .collect(Collectors.toList());

        for (Shipment shipment : overdueShipments) {
            // AI decision: Auto-reschedule or escalate
            if (shouldAutoReschedule(shipment)) {
                autoRescheduleDelivery(shipment);
            } else {
                escalateOverdueDelivery(shipment);
            }
        }
    }

    private LocalDateTime calculatePredictiveDeliveryTime(Shipment shipment) {
        // AI algorithm considering multiple factors
        LocalDateTime baseEstimate = shipment.getEstimatedDelivery();

        // Simulate real-time factors
        int trafficDelay = random.nextInt(60); // 0-60 minutes traffic
        int weatherDelay = random.nextInt(30); // 0-30 minutes weather
        int routeOptimization = -random.nextInt(20); // 0-20 minutes saved from optimization

        int totalAdjustment = trafficDelay + weatherDelay + routeOptimization;

        return baseEstimate.plusMinutes(totalAdjustment);
    }

    private boolean isSignificantTimeChange(LocalDateTime old, LocalDateTime newTime) {
        if (old == null || newTime == null) return false;
        long minutesDiff = Math.abs(java.time.Duration.between(old, newTime).toMinutes());
        return minutesDiff > 30; // Significant if more than 30 minutes difference
    }

    private int calculatePredictedDemand(Product product) {
        // AI prediction based on historical patterns - FIXED for Java 11
        int baseDemand;
        switch (product.getCategory()) {
            case "TILES":
                baseDemand = 50;
                break;
            case "CONSTRUCTION_MATERIALS":
                baseDemand = 75;
                break;
            case "ROOFING_MATERIALS":
                baseDemand = 40;
                break;
            case "PLUMBING_SUPPLIES":
                baseDemand = 30;
                break;
            default:
                baseDemand = 25;
        }

        // Add seasonal/random variations
        return baseDemand + random.nextInt(25) - 12; // ±12 variation
    }

    private int calculateOptimalReorderQuantity(Product product, int predictedDemand) {
        int currentStock = product.getStockQuantity();
        int safetyStock = predictedDemand / 2;
        int targetStock = predictedDemand * 3; // 3x predicted demand

        if (currentStock < safetyStock) {
            return targetStock - currentStock;
        }

        return 0; // No reorder needed
    }

    private void detectDemandAnomalies() {
        // AI algorithm to detect unusual demand spikes
        Map<String, Long> categoryDemand = orderRepository.findRecentOrders(LocalDateTime.now().minusHours(24))
                .stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getCategory(),
                        Collectors.summingLong(OrderItem::getQuantity)
                ));

        for (Map.Entry<String, Long> entry : categoryDemand.entrySet()) {
            String category = entry.getKey();
            Long demand = entry.getValue();
            Long normalDemand = getNormalDailyDemand(category);

            if (demand > normalDemand * 2) { // 2x normal demand
                notificationService.sendUrgentAlert("DEMAND_ANALYSIS",
                        "AI Alert: Unusual demand spike detected for " + category +
                                ". Today: " + demand + ", Normal: " + normalDemand +
                                ". Consider increasing inventory levels.");
            }
        }
    }

    private void optimizeDeliveryRoute(String truckId, List<Shipment> shipments) {
        // AI route optimization (simplified)
        List<Shipment> optimizedOrder = shipments.stream()
                .sorted((s1, s2) -> {
                    // Sort by delivery address proximity (simplified by alphabetical order)
                    return s1.getOrder().getDeliveryAddress().compareTo(s2.getOrder().getDeliveryAddress());
                })
                .collect(Collectors.toList());

        // Update estimated delivery times based on optimized route
        LocalDateTime currentTime = LocalDateTime.now();
        for (int i = 0; i < optimizedOrder.size(); i++) {
            Shipment shipment = optimizedOrder.get(i);
            LocalDateTime newEstimate = currentTime.plusHours(2 + i); // 2 hours between deliveries

            if (!newEstimate.equals(shipment.getEstimatedDelivery())) {
                shipment.setEstimatedDelivery(newEstimate);
                shipmentRepository.save(shipment);

                System.out.println("AI optimized route for truck " + truckId +
                        " - Updated delivery time for shipment " + shipment.getId());
            }
        }
    }

    private boolean shouldAutoReschedule(Shipment shipment) {
        // AI decision: Only auto-reschedule if delay is minor
        long hoursOverdue = java.time.Duration.between(shipment.getEstimatedDelivery(), LocalDateTime.now()).toHours();
        return hoursOverdue < 4; // Less than 4 hours overdue
    }

    private void autoRescheduleDelivery(Shipment shipment) {
        LocalDateTime newDelivery = LocalDateTime.now().plusHours(2);
        shipment.setEstimatedDelivery(newDelivery);
        shipmentRepository.save(shipment);

        notificationService.sendNotification(
                shipment.getOrder().getClientId(),
                "AI Auto-reschedule: Your order #" + shipment.getOrder().getId() +
                        " delivery has been rescheduled to " + newDelivery + " due to logistics optimization."
        );

        System.out.println("AI auto-rescheduled delivery for shipment " + shipment.getId());
    }

    private void escalateOverdueDelivery(Shipment shipment) {
        notificationService.sendUrgentAlert("DELIVERY_MANAGEMENT",
                "Shipment #" + shipment.getId() + " is significantly overdue. " +
                        "Original estimate: " + shipment.getEstimatedDelivery() +
                        ". Manual intervention required.");
    }

    private int getExpectedProcessingTime(OrderStatus status) {
        // Expected time in hours for each status
        switch (status) {
            case RECEIVED:
                return 2;
            case SCHEDULED:
                return 1;  // Add expected processing time for SCHEDULED status
            case VALIDATED:
                return 1;
            case INVENTORY_CHECKED:
                return 1;
            case FULFILLED:
                return 3;
            case READY_FOR_PICKUP:
                return 6;
            case IN_TRANSIT:
                return 24;
            default:
                return 12;
        }
    }


    private Long getNormalDailyDemand(String category) {
        // Historical normal demand (would come from analytics in a real system) - FIXED for Java 11
        switch (category) {
            case "TILES":
                return 200L;
            case "CONSTRUCTION_MATERIALS":
                return 150L;
            case "ROOFING_MATERIALS":
                return 100L;
            case "PLUMBING_SUPPLIES":
                return 75L;
            default:
                return 50L;
        }
    }
}