// ============= PRODUCT ENTITY =============
package com.logistics.model;

import javax.persistence.*;

/**
 * Product entity representing items available in the warehouse
 */
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String sku;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(nullable = false, length = 100)
    private String category; // TILES, CONSTRUCTION_MATERIALS, ROOFING_MATERIALS, PLUMBING_SUPPLIES
    
    @Column(nullable = false)
    private Double weight; // kg
    
    @Column(nullable = false)
    private Double volume; // m³
    
    @Column(nullable = false)
    private Integer stockQuantity;
    
    @Column(nullable = false, length = 50)
    private String location; // Warehouse location code
    
    // Constructors
    public Product() {}
    
    public Product(String sku, String name, String category, Double weight, Double volume, Integer stockQuantity, String location) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.weight = weight;
        this.volume = volume;
        this.stockQuantity = stockQuantity;
        this.location = location;
    }
    
    // Business methods
    public boolean isAvailable(Integer requestedQuantity) {
        return stockQuantity >= requestedQuantity;
    }
    
    public boolean isHeavy() {
        return weight > 50.0; // Consider items over 50kg as heavy
    }
    
    public boolean isFragile() {
        return "TILES".equals(category);
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    
    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}