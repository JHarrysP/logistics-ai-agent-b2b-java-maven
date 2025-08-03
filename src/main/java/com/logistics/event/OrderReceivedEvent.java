package com.logistics.event;

import com.logistics.model.Order;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new order is received
 */
public class OrderReceivedEvent extends ApplicationEvent {
    private final Order order;
    
    public OrderReceivedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    
    public Order getOrder() {
        return order;
    }
}