package com.logistics.service;

import com.logistics.model.*;
import com.logistics.repository.OrderRepository;
import com.logistics.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LogisticsAIAgent
 */
class LogisticsAIAgentTest {

    @Mock
    private OrderValidationAgent orderValidationAgent;

    @Mock
    private InventoryAgent inventoryAgent;

    @Mock
    private FulfillmentAgent fulfillmentAgent;

    @Mock
    private WarehouseAgent warehouseAgent;

    @Mock
    private ShippingAgent shippingAgent;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LogisticsAIAgent aiAgent;

    private Order testOrder;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testProduct = new Product("TEST-001", "Test Product", "TILES", 10.0, 0.5, 100, "A-01-01");
        testProduct.setId(1L);

        testOrder = new Order("CLIENT-001", "Test Client", "Test Address", LocalDateTime.now().plusDays(2));
        testOrder.setId(1L);
        testOrder.addItem(new OrderItem(testProduct, 5, 25.99));
    }

    @Test
    void processOrder_ValidOrder_ReturnsSuccess() throws Exception {
        // Arrange
        when(orderValidationAgent.validateOrder(any())).thenReturn(new ValidationResult(true, "Valid"));
        when(inventoryAgent.checkInventory(any())).thenReturn(new InventoryCheckResult(true, "Available"));
        when(fulfillmentAgent.fulfillOrder(any())).thenReturn(new FulfillmentResult(true, "Fulfilled"));
        when(warehouseAgent.generatePickingInstructions(any())).thenReturn(
            new WarehouseInstructions("Test instructions", false, 30));

        Shipment mockShipment = new Shipment(testOrder, "TRUCK-001", "DRIVER-001", LocalDateTime.now().plusHours(2));
        mockShipment.setId(1L);
        when(shippingAgent.scheduleShipment(any(), any())).thenReturn(mockShipment);
        when(orderRepository.save(any())).thenReturn(testOrder);

        // Act
        CompletableFuture<String> result = aiAgent.processOrder(testOrder);
        String response = result.get();

        // Assert
        assertTrue(response.contains("Order processed successfully"));
        assertTrue(response.contains("Shipment ID: 1"));

        verify(orderValidationAgent).validateOrder(testOrder);
        verify(inventoryAgent).checkInventory(testOrder);
        verify(fulfillmentAgent).fulfillOrder(testOrder);
        verify(warehouseAgent).generatePickingInstructions(testOrder);
        verify(shippingAgent).scheduleShipment(any(), any());
        verify(orderRepository, atLeast(1)).save(testOrder);
    }

    @Test
    void processOrder_ValidationFails_ReturnsCancelled() throws Exception {
        // Arrange
        when(orderValidationAgent.validateOrder(any())).thenReturn(
            new ValidationResult(false, "Invalid delivery address"));
        when(orderRepository.save(any())).thenReturn(testOrder);

        // Act
        CompletableFuture<String> result = aiAgent.processOrder(testOrder);
        String response = result.get();

        // Assert
        assertTrue(response.contains("Order cancelled"));
        assertTrue(response.contains("Invalid delivery address"));

        verify(orderValidationAgent).validateOrder(testOrder);
        verify(inventoryAgent, never()).checkInventory(any());
        verify(notificationService).sendNotification(eq("CLIENT-001"), anyString());
    }

    @Test
    void processOrder_InsufficientInventory_ReturnsCancelled() throws Exception {
        // Arrange
        when(orderValidationAgent.validateOrder(any())).thenReturn(new ValidationResult(true, "Valid"));
        when(inventoryAgent.checkInventory(any())).thenReturn(
            new InventoryCheckResult(false, "Insufficient stock"));
        when(orderRepository.save(any())).thenReturn(testOrder);

        // Act
        CompletableFuture<String> result = aiAgent.processOrder(testOrder);
        String response = result.get();

        // Assert
        assertTrue(response.contains("Order cancelled"));
        assertTrue(response.contains("Insufficient stock"));

        verify(orderValidationAgent).validateOrder(testOrder);
        verify(inventoryAgent).checkInventory(testOrder);
        verify(fulfillmentAgent, never()).fulfillOrder(any());
    }
}