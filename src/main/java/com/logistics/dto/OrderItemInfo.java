/ ============= ORDER ITEM INFO DTO =============
package com.logistics.dto;

/**
 * DTO for order item information within order status
 */
public class OrderItemInfo {
    private String sku;
    private String productName;
    private String category;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private Double weight;
    private Double volume;
    private String location;
    
    // Constructors
    public OrderItemInfo() {}
    
    public OrderItemInfo(String sku, String productName, String category, Integer quantity, 
                        Double unitPrice, Double weight, Double volume, String location) {
        this.sku = sku;
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = quantity * unitPrice;
        this.weight = weight;
        this.volume = volume;
        this.location = location;
    }
    
    // Getters and setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    
    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}