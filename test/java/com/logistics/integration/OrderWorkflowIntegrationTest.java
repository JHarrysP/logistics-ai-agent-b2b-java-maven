package com.logistics.integration;

import com.logistics.dto.OrderItemRequest;
import com.logistics.dto.OrderRequest;
import com.logistics.model.OrderStatus;
import com.logistics.repository.OrderRepository;
import com.logistics.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test for the complete order workflow
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class OrderWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void completeOrderWorkflow_ValidOrder_ProcessesSuccessfully() throws Exception {
        // Arrange
        OrderItemRequest item1 = new OrderItemRequest("TILE-001", 5, 25.99);
        OrderItemRequest item2 = new OrderItemRequest("CONC-001", 2, 15.50);
        
        OrderRequest orderRequest = new OrderRequest(
            "INTEGRATION_TEST_CLIENT",
            "Integration Test Company",
            "Test Address, Hamburg, Germany",
            LocalDateTime.now().plusDays(3),
            Arrays.asList(item1, item2)
        );

        // Act - Submit order
        mockMvc.perform(post("/api/orders/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order submitted successfully and is being processed by AI agents"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        // Wait a moment for async processing
        Thread.sleep(2000);

        // Assert - Check that order was processed
        var orders = orderRepository.findByClientId("INTEGRATION_TEST_CLIENT");
        assertFalse(orders.isEmpty());
        
        var order = orders.get(0);
        assertNotEquals(OrderStatus.RECEIVED, order.getStatus()); // Should have progressed beyond RECEIVED
        assertNotNull(order.getTotalWeight());
        assertTrue(order.getTotalWeight() > 0);
    }

    @Test
    void orderWorkflow_InvalidProduct_ReturnsError() throws Exception {
        // Arrange
        OrderItemRequest invalidItem = new OrderItemRequest("INVALID-SKU", 5, 25.99);
        
        OrderRequest orderRequest = new OrderRequest(
            "TEST_CLIENT",
            "Test Company",
            "Test Address, Hamburg, Germany", 
            LocalDateTime.now().plusDays(3),
            Arrays.asList(invalidItem)
        );

        // Act & Assert
        mockMvc.perform(post("/api/orders/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Product not found: INVALID-SKU"));
    }
}