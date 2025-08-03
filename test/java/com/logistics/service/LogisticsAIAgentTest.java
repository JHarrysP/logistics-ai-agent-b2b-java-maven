package com.logistics.service;

import com.logistics.model.Order;
import com.logistics.model.OrderStatus;
import com.logistics.repository.OrderRepository;
import com.logistics.util.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogisticsAIAgentTest {

    @Mock
    private OrderValidationAgent orderValidationAgent;
    
    @Mock
    private InventoryAgent inventoryAgent;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LogisticsAIAgent logisticsAIAgent;

    @Test
    void processOrder_ValidOrder_CompletesSuccessfully() throws Exception {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setClientId("TEST_CLIENT");

        when(orderValidationAgent.validateOrder(any(Order.class)))
            .thenReturn(new ValidationResult(true, "Valid"));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        CompletableFuture<String> result = logisticsAIAgent.processOrder(order);

        // Assert
        assertThat(result).isNotNull();
        verify(orderValidationAgent).validateOrder(order);
        verify(orderRepository, atLeastOnce()).save(order);
    }

    @Test
    void processOrder_InvalidOrder_CancelsOrder() throws Exception {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setClientId("TEST_CLIENT");

        when(orderValidationAgent.validateOrder(any(Order.class)))
            .thenReturn(new ValidationResult(false, "Invalid delivery date"));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        CompletableFuture<String> result = logisticsAIAgent.processOrder(order);

        // Assert
        String resultMessage = result.get();
        assertThat(resultMessage).contains("cancelled");
        verify(notificationService).sendNotification(eq("TEST_CLIENT"), anyString());
    }
}