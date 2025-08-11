// ============= MOVE OrderStatsResponse to DTO package =============
// File: src/main/java/com/logistics/dto/OrderStatsResponse.java
package com.logistics.dto;

/**
 * DTO for order statistics response
 */
public class OrderStatsResponse {
    private long totalOrders;
    private long receivedOrders;
    private long validatedOrders;
    private long fulfilledOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private long inTransitOrders;

    public long getInTransitOrders() { return inTransitOrders; }
    public void setInTransitOrders(long inTransitOrders) { this.inTransitOrders = inTransitOrders; }
    
    public OrderStatsResponse(long totalOrders, long receivedOrders, long validatedOrders,
                             long fulfilledOrders, long deliveredOrders, long cancelledOrders) {
        this.totalOrders = totalOrders;
        this.receivedOrders = receivedOrders;
        this.validatedOrders = validatedOrders;
        this.fulfilledOrders = fulfilledOrders;
        this.deliveredOrders = deliveredOrders;
        this.cancelledOrders = cancelledOrders;
    }
    
    // Getters and setters
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    
    public long getReceivedOrders() { return receivedOrders; }
    public void setReceivedOrders(long receivedOrders) { this.receivedOrders = receivedOrders; }
    
    public long getValidatedOrders() { return validatedOrders; }
    public void setValidatedOrders(long validatedOrders) { this.validatedOrders = validatedOrders; }
    
    public long getFulfilledOrders() { return fulfilledOrders; }
    public void setFulfilledOrders(long fulfilledOrders) { this.fulfilledOrders = fulfilledOrders; }
    
    public long getDeliveredOrders() { return deliveredOrders; }
    public void setDeliveredOrders(long deliveredOrders) { this.deliveredOrders = deliveredOrders; }
    
    public long getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }
}