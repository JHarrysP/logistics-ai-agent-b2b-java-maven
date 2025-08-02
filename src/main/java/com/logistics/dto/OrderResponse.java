// ============= ORDER RESPONSE DTO =============
package com.logistics.dto;

import java.time.LocalDateTime;

/**
 * DTO for order submission responses
 */
public class OrderResponse {
    private Long orderId;
    private String message;
    private String status;
    private LocalDateTime submittedAt;
    private String trackingReference;
    
    // Constructors
    public OrderResponse() {}
    
    public OrderResponse(Long orderId, String message, String status, LocalDateTime submittedAt) {
        this.orderId = orderId;
        this.message = message;
        this.status = status;
        this.submittedAt = submittedAt;
        this.trackingReference = "TRK-" + orderId + "-" + System.currentTimeMillis();
    }
    
    // Getters and setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    
    public String getTrackingReference() { return trackingReference; }
    public void setTrackingReference(String trackingReference) { this.trackingReference = trackingReference; }
    
    @Override
    public String toString() {
        return "OrderResponse{" +
                "orderId=" + orderId +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", submittedAt=" + submittedAt +
                ", trackingReference='" + trackingReference + '\'' +
                '}';
    }
}