// ============= WAREHOUSE AGENT =============
package com.logistics.service;

import com.logistics.model.Order;
import com.logistics.model.OrderItem;
import com.logistics.util.WarehouseInstructions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI Agent specialized in warehouse operations and picking optimization
 */
@Service
public class WarehouseAgent {

    /**
     * Generate AI-optimized picking instructions for warehouse staff
     */
    public WarehouseInstructions generatePickingInstructions(Order order) {
        System.out.println("Generating picking instructions for order: " + order.getId());

        StringBuilder instructions = new StringBuilder();
        instructions.append("PICKING INSTRUCTIONS FOR ORDER #").append(order.getId()).append("\n");
        instructions.append("Client: ").append(order.getClientName()).append("\n");
        instructions.append("Delivery: ").append(order.getDeliveryAddress()).append("\n\n");

        // Group items by warehouse location for efficient picking route
        Map<String, List<OrderItem>> itemsByLocation = order.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getLocation()));

        instructions.append("PICKING ROUTE (Optimized by AI):\n");
        instructions.append("Follow locations in this order for optimal efficiency:\n\n");

        int sequence = 1;
        int totalPickingTime = 0;

        // AI-optimized location sequence (alphabetical for simplicity, but could use graph algorithms)
        for (Map.Entry<String, List<OrderItem>> entry : itemsByLocation.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList())) {

            String location = entry.getKey();
            List<OrderItem> items = entry.getValue();

            instructions.append("Location: ").append(location).append("\n");
            instructions.append("Estimated travel time: ").append(calculateTravelTime(location)).append(" minutes\n");

            for (OrderItem item : items) {
                instructions.append(sequence++).append(". Pick ")
                        .append(item.getQuantity()).append(" x ")
                        .append(item.getProduct().getName())
                        .append(" (SKU: ").append(item.getProduct().getSku()).append(")\n");

                // Add AI-driven special handling instructions
                if (item.getProduct().isHeavy()) {
                    instructions.append("    HEAVY ITEM (").append(item.getProduct().getWeight())
                            .append("kg) - Use forklift or lifting equipment\n");
                }

                if (item.getProduct().isFragile()) {
                    instructions.append("    FRAGILE - Handle with care, use protective packaging\n");
                }

                if (item.getQuantity() > 10) {
                    instructions.append("    LARGE QUANTITY - Consider using pallet\n");
                }

                totalPickingTime += calculateItemPickingTime(item);
            }
            instructions.append("\n");
        }

        // AI-generated loading sequence optimization
        instructions.append("LOADING SEQUENCE (AI-Optimized):\n");
        instructions.append("Load items in this order for optimal truck utilization:\n");
        instructions.append("1. Heavy construction materials first (bottom of truck)\n");
        instructions.append("2. Medium weight items in middle sections\n");
        instructions.append("3. Fragile tiles last (top, with extra protection)\n");
        instructions.append("4. Small items fill remaining spaces\n\n");

        // Weight distribution analysis
        double totalWeight = order.getTotalWeight();
        if (totalWeight > 1000.0) {
            instructions.append("WEIGHT ALERT: Total order weight is ")
                    .append(String.format("%.1f", totalWeight))
                    .append("kg - Ensure truck capacity and proper weight distribution\n\n");
        }

        // Special handling requirements
        boolean requiresSpecialHandling = order.getItems().stream()
                .anyMatch(item -> item.getProduct().isHeavy() || item.getProduct().isFragile());

        if (requiresSpecialHandling) {
            instructions.append("SPECIAL HANDLING REQUIRED:\n");
            instructions.append("- Extra care needed for fragile/heavy items\n");
            instructions.append("- Additional packaging materials may be required\n");
            instructions.append("- Consider specialized transport equipment\n\n");
        }

        instructions.append("Estimated total picking time: ").append(totalPickingTime).append(" minutes\n");
        instructions.append("Order summary: ").append(order.getItems().size()).append(" items, ")
                .append(String.format("%.1f", totalWeight)).append("kg, ")
                .append(String.format("%.2f", order.getTotalVolume())).append("m³\n");

        return new WarehouseInstructions(
                instructions.toString(),
                requiresSpecialHandling,
                totalPickingTime
        );
    }

    /**
     * AI algorithm to calculate travel time between warehouse locations
     */
    private int calculateTravelTime(String location) {
        // Simple AI heuristic based on location codes
        // In real system, would use warehouse layout mapping
        char zone = location.charAt(0);
        switch (zone) {
            case 'A':
                return 2; // Zone A is closest to dispatch
            case 'B':
                return 4; // Zone B is medium distance
            case 'C':
                return 6; // Zone C is farther
            case 'D':
                return 8; // Zone D is farthest
            default:
                return 5; // Default travel time
        }
    }

    /**
     * AI algorithm to calculate picking time for individual items
     */
    private int calculateItemPickingTime(OrderItem item) {
        int baseTime = 3; // Base 3 minutes per item

        // Add time based on quantity
        if (item.getQuantity() > 5) {
            baseTime += 2; // Extra time for larger quantities
        }

        // Add time for heavy items
        if (item.getProduct().isHeavy()) {
            baseTime += 3; // Extra time for heavy item handling
        }

        // Add time for fragile items (careful handling)
        if (item.getProduct().isFragile()) {
            baseTime += 2; // Extra time for careful packaging
        }

        return baseTime;
    }
}