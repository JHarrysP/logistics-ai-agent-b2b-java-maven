package com.logistics.service;

import com.logistics.model.Order;
import com.logistics.model.OrderItem;
import com.logistics.model.Product;
import com.logistics.util.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderValidationAgent
 */
class OrderValidationAgentTest {

    private OrderValidationAgent validationAgent;
    private Order validOrder;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        validationAgent = new OrderValidationAgent();
        testProduct = new Product("TEST-001", "Test Product", "TILES", 10.0, 0.5, 100, "A-01-01");
        
        validOrder = new Order("CLIENT-001", "Test Client", 
                              "123 Test Street, Hamburg, Germany", 
                              LocalDateTime.now().plusDays(2));
        validOrder.addItem(new OrderItem(testProduct, 5, 25.99));
    }

    @Test
    void validateOrder_ValidOrder_ReturnsTrue() {
        // Act
        ValidationResult result = validationAgent.validateOrder(validOrder);

        // Assert
        assertTrue(result.isValid());
        assertEquals("Order is valid", result.getReason());
    }

    @Test
    void validateOrder_EmptyItems_ReturnsFalse() {
        // Arrange
        Order emptyOrder = new Order("CLIENT-001", "Test Client", 
                                   "123 Test Street, Hamburg, Germany", 
                                   LocalDateTime.now().plusDays(2));

        // Act
        ValidationResult result = validationAgent.validateOrder(emptyOrder);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Order contains no items", result.getReason());
    }

    @Test
    void validateOrder_InvalidDeliveryAddress_ReturnsFalse() {
        // Arrange
        validOrder.setDeliveryAddress("");

        // Act
        ValidationResult result = validationAgent.validateOrder(validOrder);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Invalid delivery address", result.getReason());
    }

    @Test
    void validateOrder_PastDeliveryDate_ReturnsFalse() {
        // Arrange
        validOrder.setRequestedDeliveryDate(LocalDateTime.now().minusDays(1));

        // Act
        ValidationResult result = validationAgent.validateOrder(validOrder);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getReason().contains("Delivery date must be at least 1 day in advance"));
    }

    @Test
    void validateOrder_NonGermanAddress_ReturnsFalse() {
        // Arrange
        validOrder.setDeliveryAddress("123 Main Street, London, UK");

        // Act
        ValidationResult result = validationAgent.validateOrder(validOrder);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getReason().contains("Delivery address must be in Germany"));
    }

    @Test
    void validateOrder_TooManyItems_ReturnsFalse() {
        // Arrange
        Order largeOrder = new Order("CLIENT-001", "Test Client", 
                                   "123 Test Street, Hamburg, Germany", 
                                   LocalDateTime.now().plusDays(2));
        
        // Add 51 items (over the limit of 50)
        for (int i = 0; i < 51; i++) {
            largeOrder.addItem(new OrderItem(testProduct, 1, 25.99));
        }

        // Act
        ValidationResult result = validationAgent.validateOrder(largeOrder);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getReason().contains("Order too large"));
    }
}