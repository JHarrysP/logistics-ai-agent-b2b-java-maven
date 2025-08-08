// ============= ORDER ENTITY =============
package com.logistics.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Order entity representing a B2B logistics order
 */
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String clientId;
    
    @Column(nullable = false)
    private String clientName;
    
    @Column(nullable = false)
    private LocalDateTime orderDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();
    
    @Column(nullable = false, length = 500)
    private String deliveryAddress;
    
    @Column(nullable = false)
    private LocalDateTime requestedDeliveryDate;
    
    @Column(nullable = false)
    private Double totalWeight = 0.0;
    
    @Column(nullable = false)
    private Double totalVolume = 0.0;
    
    // Constructors
    public Order() {}
    
    public Order(String clientId, String clientName, String deliveryAddress, LocalDateTime requestedDeliveryDate) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.deliveryAddress = deliveryAddress;
        this.requestedDeliveryDate = requestedDeliveryDate;
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.RECEIVED;
        this.totalWeight = 0.0;
        this.totalVolume = 0.0;
    }
    
    // Business methods
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        updateTotals();
    }
    
    private void updateTotals() {
        this.totalWeight = items.stream()
            .mapToDouble(item -> item.getQuantity() * item.getProduct().getWeight())
            .sum();
        this.totalVolume = items.stream()
            .mapToDouble(item -> item.getQuantity() * item.getProduct().getVolume())
            .sum();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public LocalDateTime getRequestedDeliveryDate() { return requestedDeliveryDate; }
    public void setRequestedDeliveryDate(LocalDateTime requestedDeliveryDate) { this.requestedDeliveryDate = requestedDeliveryDate; }
    
    public Double getTotalWeight() { return totalWeight; }
    public void setTotalWeight(Double totalWeight) { this.totalWeight = totalWeight; }
    
    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }
}
