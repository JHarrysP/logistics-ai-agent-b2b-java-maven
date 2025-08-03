package com.logistics.integration;

import com.logistics.LogisticsAIAgentApplication;
import com.logistics.dto.OrderRequest;
import com.logistics.dto.OrderItemRequest;
import com.logistics.repository.OrderRepository;
import com.logistics.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LogisticsAIAgentApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class OrderProcessingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void fullOrderProcessingWorkflow_IntegrationTest() throws Exception {
        // Arrange
        OrderItemRequest item = new OrderItemRequest("TILE-001", 5, 25.99);
        OrderRequest request = new OrderRequest(
            "CLIENT_INTEGRATION_001",
            "Integration Test Client",
            "Integration Test Address, Hamburg, Germany",
            LocalDateTime.now().plusDays(3),
            Arrays.asList(item)
        );

        // Act & Assert
        mockMvc.perform(post("/api/orders/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.message").value(containsString("successfully")));

        // Verify order was saved
        assertThat(orderRepository.count()).isGreaterThan(0);
    }
}