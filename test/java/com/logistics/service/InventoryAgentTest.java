package com.logistics.service;

import com.logistics.model.Order;
import com.logistics.model.OrderItem;
import com.logistics.model.Product;
import com.logistics.repository.ProductRepository;
import com.logistics.util.InventoryCheckResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for InventoryAgent  
 */
class InventoryAgentTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryAgent inventoryAgent;

    private Order testOrder;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testProduct = new Product("TEST-001", "Test Product", "TILES", 10.0, 0.5, 100, "A-01-01");
        testProduct.setId(1L);
        
        testOrder = new Order("CLIENT-001", "Test Client", "Test Address", LocalDateTime.now().plusDays(2));
        testOrder.addItem(new OrderItem(testProduct, 10, 25.99));
    }

    @Test
    void checkInventory_SufficientStock_ReturnsTrue() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        InventoryCheckResult result = inventoryAgent.checkInventory(testOrder);

        // Assert
        assertTrue(result.isAvailable());
        assertEquals("All items available in sufficient quantity", result.getMessage());
    }

    @Test
    void checkInventory_InsufficientStock_ReturnsFalse() {
        // Arrange
        testProduct.setStockQuantity(5); // Less than requested 10
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        InventoryCheckResult result = inventoryAgent.checkInventory(testOrder);

        // Assert
        assertFalse(result.isAvailable());
        assertTrue(result.getMessage().contains("Insufficient stock"));
        assertTrue(result.getMessage().contains("Available: 5"));
        assertTrue(result.getMessage().contains("Requested: 10"));
    }

    @Test
    void checkInventory_ProductNotFound_ReturnsFalse() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        InventoryCheckResult result = inventoryAgent.checkInventory(testOrder);

        // Assert
        assertFalse(result.isAvailable());
        assertTrue(result.getMessage().contains("Product not found"));
    }
}