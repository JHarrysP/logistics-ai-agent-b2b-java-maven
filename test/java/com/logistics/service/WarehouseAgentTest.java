package com.logistics.service;

import com.logistics.model.Order;
import com.logistics.model.OrderItem;
import com.logistics.model.Product;
import com.logistics.util.WarehouseInstructions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WarehouseAgent
 */
class WarehouseAgentTest {

    private WarehouseAgent warehouseAgent;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        warehouseAgent = new WarehouseAgent();
        
        testOrder = new Order("CLIENT-001", "Test Client", "Test Address", LocalDateTime.now().plusDays(2));
        testOrder.setId(1L);
        
        // Add items from different locations
        Product tileProduct = new Product("TILE-001", "Ceramic Tiles", "TILES", 25.0, 0.5, 100, "A-01-01");
        Product cementProduct = new Product("CONC-001", "Portland Cement", "CONSTRUCTION_MATERIALS", 50.0, 0.4, 100, "B-02-01");
        
        testOrder.addItem(new OrderItem(tileProduct, 10, 25.99));
        testOrder.addItem(new OrderItem(cementProduct, 5, 15.50));
    }

    @Test
    void generatePickingInstructions_ValidOrder_ReturnsInstructions() {
        // Act
        WarehouseInstructions instructions = warehouseAgent.generatePickingInstructions(testOrder);

        // Assert
        assertNotNull(instructions);
        assertNotNull(instructions.getInstructions());
        assertTrue(instructions.getInstructions().contains("PICKING INSTRUCTIONS"));
        assertTrue(instructions.getInstructions().contains("Location: A-01-01"));
        assertTrue(instructions.getInstructions().contains("Location: B-02-01"));
        assertTrue(instructions.getInstructions().contains("LOADING SEQUENCE"));
        assertTrue(instructions.getEstimatedPickingTime() > 0);
    }

    @Test
    void generatePickingInstructions_HeavyItems_RequiresSpecialHandling() {
        // Arrange - Add very heavy item
        Product heavyProduct = new Product("HEAVY-001", "Heavy Steel", "CONSTRUCTION_MATERIALS", 2000.0, 2.0, 10, "B-02-02");
        testOrder.addItem(new OrderItem(heavyProduct, 1, 500.0));

        // Act
        WarehouseInstructions instructions = warehouseAgent.generatePickingInstructions(testOrder);

        // Assert
        assertTrue(instructions.requiresSpecialHandling());
        assertTrue(instructions.getInstructions().contains("HEAVY ITEM"));
        assertTrue(instructions.getInstructions().contains("Use forklift"));
    }

    @Test
    void generatePickingInstructions_FragileItems_ContainsFragileWarning() {
        // Act
        WarehouseInstructions instructions = warehouseAgent.generatePickingInstructions(testOrder);

        // Assert
        assertTrue(instructions.getInstructions().contains("FRAGILE"));
        assertTrue(instructions.getInstructions().contains("Handle with care"));
    }
}