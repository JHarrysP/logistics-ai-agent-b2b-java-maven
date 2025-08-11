package com.logistics.controller;

import com.logistics.dto.*;
import com.logistics.model.*;
import com.logistics.repository.*;
import com.logistics.service.LogisticsAIAgent;
import com.logistics.service.RealtimeNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Enhanced REST Controller for order management operations with WebSocket integration
 */
@RestController
@RequestMapping("/api/orders")
@Validated
@Tag(name = "Orders", description = "Order Management API - Submit, track, and manage B2B logistics orders")
public class OrderController {

    @Autowired
    private LogisticsAIAgent aiAgent;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private RealtimeNotificationService notificationService;

    /**
     * Submit a new order for AI-powered processing
     */
    @PostMapping("/submit")
    @Operation(summary = "Submit Order",
            description = "Submit a new B2B logistics order for AI-powered processing and fulfillment")
    @ApiResponse(responseCode = "200", description = "Order submitted successfully")
    @ApiResponse(responseCode = "400", description = "Invalid order data")
    @ApiResponse(responseCode = "422", description = "Business validation failed")
    @Transactional
    public ResponseEntity<OrderResponse> submitOrder(@Valid @RequestBody OrderRequest request) {
        try {
            System.out.println(" Received order submission: " + request);

            // Create order entity
            Order order = new Order(request.getClientId(), request.getClientName(),
                    request.getDeliveryAddress(), request.getRequestedDeliveryDate());

            // Add and validate items
            for (OrderItemRequest itemRequest : request.getItems()) {
                Product product = productRepository.findBySku(itemRequest.getSku()).orElse(null);
                if (product == null) {
                    return ResponseEntity.badRequest()
                            .body(new OrderResponse(null, "Product not found: " + itemRequest.getSku(),
                                    "REJECTED", null));
                }

                OrderItem item = new OrderItem(product, itemRequest.getQuantity(), itemRequest.getUnitPrice());
                order.addItem(item);
            }

            // Save order
            order = orderRepository.save(order);
            System.out.println(" Order saved with ID: " + order.getId());

            // Send real-time notification
            notificationService.sendNewOrderNotification(
                    order.getId(),
                    order.getClientName(),
                    order.getItems().size()
            );

            // Process asynchronously with AI Agent
            CompletableFuture<String> processingResult = aiAgent.processOrder(order);

            // Return immediate response
            OrderResponse response = new OrderResponse(
                    order.getId(),
                    "Order submitted successfully and is being processed by AI agents",
                    order.getStatus().toString(),
                    order.getOrderDate()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println(" Error submitting order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new OrderResponse(null, "Error submitting order: " + e.getMessage(),
                            "ERROR", null));
        }
    }

    /**
     * Get detailed order status and tracking information
     */
    @GetMapping("/{orderId}/status")
    @Operation(summary = "Get Order Status",
            description = "Retrieve detailed status and tracking information for a specific order")
    @ApiResponse(responseCode = "200", description = "Order status retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderStatusResponse> getOrderStatus(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId) {

        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }

            OrderStatusResponse response = buildOrderStatusResponse(order);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println(" Error getting order status for order " + orderId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all orders for a specific client
     */
    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get Client Orders",
            description = "Retrieve all orders for a specific client, ordered by date descending")
    @ApiResponse(responseCode = "200", description = "Client orders retrieved successfully")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderStatusResponse>> getClientOrders(
            @Parameter(description = "Client ID", required = true) @PathVariable String clientId) {

        try {
            List<Order> orders = orderRepository.findByClientIdOrderByOrderDateDesc(clientId);
            List<OrderStatusResponse> responses = orders.stream()
                    .map(this::buildOrderStatusResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            System.err.println(" Error getting client orders for " + clientId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get orders by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get Orders by Status",
            description = "Retrieve all orders with a specific status")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderStatusResponse>> getOrdersByStatus(
            @Parameter(description = "Order Status", required = true) @PathVariable String status) {

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderRepository.findByStatus(orderStatus);
            List<OrderStatusResponse> responses = orders.stream()
                    .map(this::buildOrderStatusResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println(" Error getting orders by status " + status + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cancel an existing order
     */
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel Order",
            description = "Cancel an existing order if it hasn't been shipped yet")
    @ApiResponse(responseCode = "200", description = "Order cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "400", description = "Cannot cancel order in current status")
    @Transactional
    public ResponseEntity<String> cancelOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId) {

        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if order can be cancelled
            if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.IN_TRANSIT) {
                return ResponseEntity.badRequest()
                        .body("Cannot cancel order in status: " + order.getStatus().getDescription());
            }

            OrderStatus oldStatus = order.getStatus();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            // Send real-time status update
            notificationService.sendOrderStatusUpdate(orderId, oldStatus.toString(), "CANCELLED");

            System.out.println(" Order cancelled: " + orderId);

            return ResponseEntity.ok("Order #" + orderId + " cancelled successfully");

        } catch (Exception e) {
            System.err.println(" Error cancelling order " + orderId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cancelling order: " + e.getMessage());
        }
    }

    /**
     * Mark order as delivered
     */
    @PostMapping("/{orderId}/delivered")
    @Operation(summary = "Mark Order as Delivered",
            description = "Mark an order as successfully delivered")
    @ApiResponse(responseCode = "200", description = "Order marked as delivered")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "400", description = "Invalid status transition")
    @Transactional
    public ResponseEntity<String> markOrderDelivered(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId) {

        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if order can be marked as delivered
            if (order.getStatus() != OrderStatus.IN_TRANSIT) {
                return ResponseEntity.badRequest()
                        .body("Cannot mark as delivered - order must be in transit. Current status: " +
                                order.getStatus().getDescription());
            }

            OrderStatus oldStatus = order.getStatus();
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);

            // Update shipment if exists
            List<Shipment> shipments = shipmentRepository.findByOrderId(orderId);
            if (!shipments.isEmpty()) {
                Shipment shipment = shipments.get(0);
                shipment.setStatus(ShipmentStatus.DELIVERED);
                shipment.setActualDelivery(LocalDateTime.now());
                shipmentRepository.save(shipment);
            }

            // Send real-time notifications
            notificationService.sendOrderStatusUpdate(orderId, oldStatus.toString(), "DELIVERED");

            notificationService.sendNotification(order.getClientId(),
                    "Order #" + orderId + " has been delivered successfully. Thank you for your business!");

            notificationService.sendInternalNotification("DELIVERY",
                    "Order #" + orderId + " delivered to " + order.getClientName() +
                            " at " + LocalDateTime.now());

            System.out.println(" Order delivered: " + orderId);

            return ResponseEntity.ok("Order #" + orderId + " marked as delivered successfully");

        } catch (Exception e) {
            System.err.println(" Error marking order as delivered " + orderId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error marking order as delivered: " + e.getMessage());
        }
    }

    /**
     * Get order summary statistics with real-time updates
     */
    @GetMapping("/stats")
    @Operation(summary = "Get Order Statistics",
            description = "Retrieve summary statistics about orders")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderStatsResponse> getOrderStats() {
        try {
            long totalOrders = orderRepository.count();
            long receivedOrders = orderRepository.countByStatus(OrderStatus.RECEIVED);
            long validatedOrders = orderRepository.countByStatus(OrderStatus.VALIDATED);
            long fulfilledOrders = orderRepository.countByStatus(OrderStatus.FULFILLED);
            long inTransitOrders = orderRepository.countByStatus(OrderStatus.IN_TRANSIT);
            long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
            long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

            OrderStatsResponse stats = new OrderStatsResponse(
                    totalOrders, receivedOrders, validatedOrders, fulfilledOrders,
                    deliveredOrders, cancelledOrders
            );

            // Add additional stats for enhanced dashboard
            stats.setInTransitOrders(inTransitOrders);

            // Send real-time stats update
            notificationService.sendStatsUpdate(java.util.Map.of(
                    "totalOrders", totalOrders,
                    "receivedOrders", receivedOrders,
                    "inTransitOrders", inTransitOrders,
                    "deliveredOrders", deliveredOrders,
                    "cancelledOrders", cancelledOrders
            ));

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            System.err.println(" Error getting order stats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Helper method to build detailed order status response
     * This method runs within the transaction context of the calling method
     */
    private OrderStatusResponse buildOrderStatusResponse(Order order) {
        try {
            OrderStatusResponse response = new OrderStatusResponse(
                    order.getId(),
                    order.getStatus().toString(),
                    order.getClientName(),
                    order.getOrderDate(),
                    order.getRequestedDeliveryDate(),
                    order.getTotalWeight(),
                    order.getTotalVolume()
            );

            response.setClientId(order.getClientId());
            response.setStatusDescription(order.getStatus().getDescription());

            // Safely handle order items with null checks
            List<OrderItem> items = order.getItems();
            if (items != null && !items.isEmpty()) {
                response.setTotalItems(items.size());

                // Add item details with null safety
                List<OrderItemInfo> itemInfos = items.stream()
                        .filter(item -> item != null && item.getProduct() != null)
                        .map(item -> {
                            try {
                                return new OrderItemInfo(
                                        item.getProduct().getSku(),
                                        item.getProduct().getName(),
                                        item.getProduct().getCategory(),
                                        item.getQuantity(),
                                        item.getUnitPrice(),
                                        item.getProduct().getWeight(),
                                        item.getProduct().getVolume(),
                                        item.getProduct().getLocation()
                                );
                            } catch (Exception e) {
                                System.err.println(" Error creating OrderItemInfo for item " +
                                        (item != null ? item.getId() : "null") + ": " + e.getMessage());
                                return null;
                            }
                        })
                        .filter(itemInfo -> itemInfo != null)
                        .collect(Collectors.toList());
                response.setItems(itemInfos);
            } else {
                response.setTotalItems(0);
                response.setItems(new ArrayList<>());
            }

            // Add shipment information if available
            try {
                List<Shipment> shipments = shipmentRepository.findByOrderId(order.getId());
                if (shipments != null && !shipments.isEmpty()) {
                    Shipment shipment = shipments.get(0); // Assuming one shipment per order
                    ShipmentInfo shipmentInfo = new ShipmentInfo(
                            shipment.getId(),
                            shipment.getTruckId(),
                            shipment.getDriverId(),
                            shipment.getStatus().toString(),
                            shipment.getScheduledPickup(),
                            shipment.getEstimatedDelivery()
                    );
                    shipmentInfo.setActualPickup(shipment.getActualPickup());
                    shipmentInfo.setActualDelivery(shipment.getActualDelivery());
                    shipmentInfo.setRequiresSpecialHandling(shipment.getRequiresSpecialHandling());
                    response.setShipmentInfo(shipmentInfo);
                    response.setEstimatedDelivery(shipment.getEstimatedDelivery());
                }
            } catch (Exception e) {
                System.err.println(" Error loading shipment info for order " + order.getId() + ": " + e.getMessage());
                // Continue without shipment info rather than failing
            }

            return response;

        } catch (Exception e) {
            System.err.println(" Error in buildOrderStatusResponse for order " +
                    (order != null ? order.getId() : "null") + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to build order status response", e);
        }
    }
}