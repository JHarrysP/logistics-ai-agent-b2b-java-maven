package com.logistics.event;

import com.logistics.model.Order;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an order is successfully validated
 */
public class OrderValidatedEvent extends ApplicationEvent {
    private final Order order;
    
    public OrderValidatedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    
    public Order getOrder() {
        return order;
    }
}