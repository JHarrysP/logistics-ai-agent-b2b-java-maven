// ============= FULFILLMENT AGENT =============
package com.logistics.service;

import com.logistics.model.Order;
import com.logistics.model.OrderItem;
import com.logistics.model.Product;
import com.logistics.repository.ProductRepository;
import com.logistics.util.FulfillmentResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI Agent specialized in order fulfillment
 */
@Service
public class FulfillmentAgent {
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Fulfill order by reserving inventory
     */
    @Transactional
    public FulfillmentResult fulfillOrder(Order order) {
        System.out.println("🎯 Fulfilling order: " + order.getId());
        
        try {
            // Reserve inventory for each item
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
                
                if (product == null) {
                    throw new RuntimeException("Product not found: " + item.getProduct().getSku());
                }
                
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for: " + product.getName());
                }
                
                // Reserve inventory
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepository.save(product);
                
                System.out.println("📦 Reserved " + item.getQuantity() + " units of " + product.getName());
            }
            
            return new FulfillmentResult(true, "Order fulfilled successfully");
            
        } catch (Exception e) {
            return new FulfillmentResult(false, "Fulfillment failed: " + e.getMessage());
        }
    }
}