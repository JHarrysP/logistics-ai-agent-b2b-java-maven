// ============= INVENTORY AGENT =============
package com.logistics.service;

import com.logistics.model.Order;
import com.logistics.model.OrderItem;
import com.logistics.model.Product;
import com.logistics.repository.ProductRepository;
import com.logistics.util.InventoryCheckResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI Agent specialized in inventory management
 */
@Service
public class InventoryAgent {
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Check inventory availability for order items
     */
    public InventoryCheckResult checkInventory(Order order) {
        System.out.println(" Checking inventory for order: " + order.getId());
        
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
            
            if (product == null) {
                return new InventoryCheckResult(false, 
                    "Product not found: " + item.getProduct().getSku());
            }
            
            if (product.getStockQuantity() < item.getQuantity()) {
                return new InventoryCheckResult(false, 
                    "Insufficient stock for product: " + product.getName() + 
                    ". Available: " + product.getStockQuantity() + 
                    ", Requested: " + item.getQuantity());
            }
            
            // AI-driven check: Reserve safety stock
            int safetyStock = calculateSafetyStock(product);
            if (product.getStockQuantity() - item.getQuantity() < safetyStock) {
                System.out.println(" Low stock warning for: " + product.getName());
                // Don't fail, just warn - could trigger reorder in real system
            }
        }
        
        return new InventoryCheckResult(true, "All items available in sufficient quantity");
    }
    
    /**
     * AI algorithm to calculate safety stock based on product characteristics
     */
    private int calculateSafetyStock(Product product) {
        // AI-driven safety stock calculation
        int baseStock = 10; // Minimum safety stock
        
        // Increase safety stock for popular categories
        if ("TILES".equals(product.getCategory())) {
            baseStock += 20; // Tiles are popular
        }
        
        if ("CONSTRUCTION_MATERIALS".equals(product.getCategory())) {
            baseStock += 15; // Construction materials have steady demand
        }
        
        // Increase safety stock for heavy items (longer lead times)
        if (product.getWeight() > 50.0) {
            baseStock += 10;
        }
        
        return baseStock;
    }
}