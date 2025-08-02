// ============= ORDER REQUEST DTO =============
package com.logistics.dto;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for order submission requests
 */
public class OrderRequest {
    
    @NotBlank(message = "Client ID is required")
    @Size(max = 50, message = "Client ID must not exceed 50 characters")
    private String clientId;
    
    @NotBlank(message = "Client name is required")
    @Size(max = 200, message = "Client name must not exceed 200 characters")
    private String clientName;
    
    @NotBlank(message = "Delivery address is required")
    @Size(max = 500, message = "Delivery address must not exceed 500 characters")
    private String deliveryAddress;
    
    @NotNull(message = "Requested delivery date is required")
    @Future(message = "Delivery date must be in the future")
    private LocalDateTime requestedDeliveryDate;
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;
    
    // Constructors
    public OrderRequest() {}
    
    public OrderRequest(String clientId, String clientName, String deliveryAddress, 
                       LocalDateTime requestedDeliveryDate, List<OrderItemRequest> items) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.deliveryAddress = deliveryAddress;
        this.requestedDeliveryDate = requestedDeliveryDate;
        this.items = items;
    }
    
    // Getters and setters
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public LocalDateTime getRequestedDeliveryDate() { return requestedDeliveryDate; }
    public void setRequestedDeliveryDate(LocalDateTime requestedDeliveryDate) { this.requestedDeliveryDate = requestedDeliveryDate; }
    
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    
    @Override
    public String toString() {
        return "OrderRequest{" +
                "clientId='" + clientId + '\'' +
                ", clientName='" + clientName + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", requestedDeliveryDate=" + requestedDeliveryDate +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                '}';
    }
}