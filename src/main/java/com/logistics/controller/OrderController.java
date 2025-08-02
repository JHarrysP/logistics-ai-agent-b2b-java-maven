package com.logistics.controller;

import com.logistics.dto.*;
import com.logistics.model.*;
import com.logistics.repository.*;
import com.logistics.service.LogisticsAIAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * REST Controller for order management operations
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
    
    /**
     * Submit a new order for AI-powered processing
     */
    @PostMapping("/submit")
    @Operation(summary = "Submit Order", 
               description = "Submit a new B2B logistics order for AI-powered processing and fulfillment")
    @ApiResponse(responseCode = "200", description = "Order submitted successfully")
    @ApiResponse(responseCode = "400", description = "Invalid order data")
    @ApiResponse(responseCode = "422", description = "Business validation failed")
    public ResponseEntity<OrderResponse> submitOrder(@Valid @RequestBody OrderRequest request) {
        try {
            System.out.println("Received order submission: " + request);
            
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
            System.out.println("Order saved with ID: " + order.getId());
            
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
    public ResponseEntity<OrderStatusResponse> getOrderStatus(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId) {
        
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        OrderStatusResponse response = buildOrderStatusResponse(order);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all orders for a specific client
     */
    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get Client Orders", 
               description = "Retrieve all orders for a specific client, ordered by date descending")
    @ApiResponse(responseCode = "200", description = "Client orders retrieved successfully")
    public ResponseEntity<List<OrderStatusResponse>> getClientOrders(
            @Parameter(description = "Client ID", required = true) @PathVariable String clientId) {
        
        List<Order> orders = orderRepository.findByClientIdOrderByOrderDateDesc(clientId);
        List<OrderStatusResponse> responses = orders.stream()
            .map(this::buildOrderStatusResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get orders by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get Orders by Status", 
               description = "Retrieve all orders with a specific status")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status")
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
    public ResponseEntity<String> cancelOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId) {
        
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Check if order can be cancelled
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.IN_TRANSIT) {
            return ResponseEntity.badRequest()
                .body("Cannot cancel order in status: " + order.getStatus().getDescription());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        System.out.println("Order cancelled: " + orderId);
        
        return ResponseEntity.ok("Order #" + orderId + " cancelled successfully");
    }
    
    /**
     * Get order summary statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get Order Statistics", 
               description = "Retrieve summary statistics about orders")
    public ResponseEntity<OrderStatsResponse> getOrderStats() {
        long totalOrders = orderRepository.count();
        long receivedOrders = orderRepository.countByStatus(OrderStatus.RECEIVED);
        long validatedOrders = orderRepository.countByStatus(OrderStatus.VALIDATED);
        long fulfilledOrders = orderRepository.countByStatus(OrderStatus.FULFILLED);
        long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);
        
        OrderStatsResponse stats = new OrderStatsResponse(
            totalOrders, receivedOrders, validatedOrders, fulfilledOrders, 
            deliveredOrders, cancelledOrders
        );
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Helper method to build detailed order status response
     */
    private OrderStatusResponse buildOrderStatusResponse(Order order) {
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
        response.setTotalItems(order.getItems().size());
        
        // Add shipment information if available
        List<Shipment> shipments = shipmentRepository.findByOrderId(order.getId());
        if (!shipments.isEmpty()) {
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
        
        // Add item details
        List<OrderItemInfo> itemInfos = order.getItems().stream()
            .map(item -> new OrderItemInfo(
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getProduct().getCategory(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getProduct().getWeight(),
                item.getProduct().getVolume(),
                item.getProduct().getLocation()
            ))
            .collect(Collectors.toList());
        response.setItems(itemInfos);
        
        return response;
    }
}