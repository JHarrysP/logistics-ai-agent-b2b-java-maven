// ============= ORDER VALIDATION AGENT =============
package com.logistics.service;

import com.logistics.model.Order;
import com.logistics.util.ValidationResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AI Agent specialized in order validation
 */
@Service
public class OrderValidationAgent {
    
    /**
     * Validate order using AI-driven business rules
     */
    public ValidationResult validateOrder(Order order) {
        System.out.println("🔍 Validating order: " + order.getId());
        
        // Check if order has items
        if (order.getItems().isEmpty()) {
            return new ValidationResult(false, "Order contains no items");
        }
        
        // Validate delivery address
        if (order.getDeliveryAddress() == null || order.getDeliveryAddress().trim().isEmpty()) {
            return new ValidationResult(false, "Invalid delivery address");
        }
        
        // Check delivery date is not in the past (allow same day delivery)
        if (order.getRequestedDeliveryDate().isBefore(LocalDateTime.now().toLocalDate().atStartOfDay())) {
            return new ValidationResult(false, "Delivery date cannot be in the past");
        }

        // Validate client information
        if (order.getClientId() == null || order.getClientId().trim().isEmpty()) {
            return new ValidationResult(false, "Client ID is required");
        }

        if (order.getClientName() == null || order.getClientName().trim().isEmpty()) {
            return new ValidationResult(false, "Client name is required");
        }
        
        // Validate delivery address format (basic check for German addresses)
        String address = order.getDeliveryAddress().toLowerCase();
        if (!address.contains("germany") && !address.contains("deutschland") && 
            !address.contains("hamburg") && !address.contains("berlin") && 
            !address.contains("munich") && !address.contains("köln")) {
            return new ValidationResult(false, "Delivery address must be in Germany");
        }
        
        return new ValidationResult(true, "Order is valid");
    }
}