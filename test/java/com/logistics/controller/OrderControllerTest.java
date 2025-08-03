package com.logistics.controller;

import com.logistics.dto.OrderRequest;
import com.logistics.dto.OrderItemRequest;
import com.logistics.model.Order;
import com.logistics.model.OrderStatus;
import com.logistics.repository.OrderRepository;
import com.logistics.service.LogisticsAIAgent;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    void submitOrder_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        OrderItemRequest item = new OrderItemRequest("TILE-001", 10, 25.99);
        OrderRequest request = new OrderRequest(
            "CLIENT_TEST_001",
            "Test Client GmbH",
            "Test Address, Hamburg, Germany",
            LocalDateTime.now().plusDays(3),
            Arrays.asList(item)
        );

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setStatus(OrderStatus.RECEIVED);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(aiAgent.processOrder(any(Order.class)))
            .thenReturn(CompletableFuture.completedFuture("Order processed successfully"));

        // Act & Assert
        mockMvc.perform(post("/api/orders/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void getOrderStatus_ExistingOrder_ReturnsStatus() throws Exception {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.VALIDATED);
        order.setClientName("Test Client");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        mockMvc.perform(get("/api/orders/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("VALIDATED"));
    }
}