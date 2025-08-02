// ============= ORDER ITEM REQUEST DTO =============
package com.logistics.dto;

import javax.validation.constraints.*;

/**
 * DTO for individual order items within an order request
 */
public class OrderItemRequest {
    
    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10000, message = "Quantity cannot exceed 10,000")
    private Integer quantity;
    
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format - max 8 integer digits and 2 decimal places")
    private Double unitPrice;
    
    // Constructors
    public OrderItemRequest() {}
    
    public OrderItemRequest(String sku, Integer quantity, Double unitPrice) {
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    // Business methods
    public Double getTotalPrice() {
        return quantity != null && unitPrice != null ? quantity * unitPrice : 0.0;
    }
    
    // Getters and setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    
    @Override
    public String toString() {
        return "OrderItemRequest{" +
                "sku='" + sku + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
}