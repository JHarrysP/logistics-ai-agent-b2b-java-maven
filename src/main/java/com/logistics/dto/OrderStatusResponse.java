// ============= ORDER STATUS RESPONSE DTO =============
package com.logistics.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for detailed order status responses
 */
public class OrderStatusResponse {
    private Long orderId;
    private String status;
    private String statusDescription;
    private String clientId;
    private String clientName;
    private LocalDateTime orderDate;
    private LocalDateTime requestedDeliveryDate;
    private LocalDateTime estimatedDelivery;
    private Double totalWeight;
    private Double totalVolume;
    private Integer totalItems;
    private ShipmentInfo shipmentInfo;
    private List<OrderItemInfo> items;
    
    // Constructors
    public OrderStatusResponse() {}
    
    public OrderStatusResponse(Long orderId, String status, String clientName, 
                             LocalDateTime orderDate, LocalDateTime requestedDeliveryDate,
                             Double totalWeight, Double totalVolume) {
        this.orderId = orderId;
        this.status = status;
        this.clientName = clientName;
        this.orderDate = orderDate;
        this.requestedDeliveryDate = requestedDeliveryDate;
        this.totalWeight = totalWeight;
        this.totalVolume = totalVolume;
    }
    
    // Getters and setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public LocalDateTime getRequestedDeliveryDate() { return requestedDeliveryDate; }
    public void setRequestedDeliveryDate(LocalDateTime requestedDeliveryDate) { this.requestedDeliveryDate = requestedDeliveryDate; }
    
    public LocalDateTime getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    
    public Double getTotalWeight() { return totalWeight; }
    public void setTotalWeight(Double totalWeight) { this.totalWeight = totalWeight; }
    
    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }
    
    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    
    public ShipmentInfo getShipmentInfo() { return shipmentInfo; }
    public void setShipmentInfo(ShipmentInfo shipmentInfo) { this.shipmentInfo = shipmentInfo; }
    
    public List<OrderItemInfo> getItems() { return items; }
    public void setItems(List<OrderItemInfo> items) { this.items = items; }
}

