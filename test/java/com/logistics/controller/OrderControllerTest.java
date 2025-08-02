package com.logistics.controller;

import com.logistics.dto.OrderRequest;
import com.logistics.dto.OrderItemRequest;
import com.logistics.model.Order;
import com.logistics.model.Product;
import com.logistics.repository.OrderRepository;
import com.logistics.repository.ProductRepository;
import com.logistics.service.LogisticsAIAgent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for OrderController
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LogisticsAIAgent aiAgent;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private ProductRepository productRepository;

    private Product testProduct;
    private Order testOrder;
    private OrderRequest validOrderRequest;

    @BeforeEach
    void setUp() {
        // Create test product
        testProduct = new Product("TEST-001", "Test Product", "TILES", 10.0, 0.5, 100, "A-01-01");
        testProduct.setId(1L);

        // Create test order
        testOrder = new Order("CLIENT-001", "Test Client", "Test Address", LocalDateTime.now().plusDays(2));
        testOrder.setId(1L);

        // Create valid order request
        OrderItemRequest itemRequest = new OrderItemRequest("TEST-001", 5, 25.99);
        validOrderRequest = new OrderRequest("CLIENT-001", "Test Client", "Test Address", 
                                           LocalDateTime.now().plusDays(2), Arrays.asList(itemRequest));
    }

    @Test
    void submitOrder_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        when(productRepository.findBySku(anyString())).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(aiAgent.processOrder(any(Order.class))).thenReturn(CompletableFuture.completedFuture("Success"));

        // Act & Assert
        mockMvc.perform(post("/api/orders/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.message").value("Order submitted successfully and is being processed by AI agents"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void submitOrder_InvalidProduct_ReturnsBadRequest() throws Exception {
        // Arrange
        when(productRepository.findBySku(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/orders/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Product not found: TEST-001"));
    }

    @Test
    void submitOrder_EmptyItems_ReturnsBadRequest() throws Exception {
        // Arrange
        OrderRequest invalidRequest = new OrderRequest("CLIENT-001", "Test Client", "Test Address", 
                                                     LocalDateTime.now().plusDays(2), Arrays.asList());

        // Act & Assert
        mockMvc.perform(post("/api/orders/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderStatus_ExistingOrder_ReturnsOrderDetails() throws Exception {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        mockMvc.perform(get("/api/orders/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.clientName").value("Test Client"));
    }

    @Test
    void getOrderStatus_NonExistentOrder_ReturnsNotFound() throws Exception {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/orders/999/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelOrder_ValidOrder_ReturnsSuccess() throws Exception {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act & Assert
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order #1 cancelled successfully"));
    }
}