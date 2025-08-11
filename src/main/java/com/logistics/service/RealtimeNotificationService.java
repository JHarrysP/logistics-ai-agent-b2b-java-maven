package com.logistics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Enhanced notification service with real-time WebSocket support
 */
@Service
public class RealtimeNotificationService extends NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Send real-time notification to dashboard
     */
    public void sendRealtimeUpdate(String type, Object data) {
        try {
            Map<String, Object> message = Map.of(
                    "type", type,
                    "data", data,
                    "timestamp", LocalDateTime.now().toString()
            );

            // Send to all connected dashboard clients
            messagingTemplate.convertAndSend("/topic/updates", message);

            System.out.println("📡 Real-time update sent: " + type);

        } catch (Exception e) {
            System.err.println("Error sending real-time update: " + e.getMessage());
        }
    }

    /**
     * Send order status update
     */
    public void sendOrderStatusUpdate(Long orderId, String oldStatus, String newStatus) {
        Map<String, Object> update = Map.of(
                "orderId", orderId,
                "oldStatus", oldStatus,
                "newStatus", newStatus,
                "timestamp", LocalDateTime.now()
        );

        sendRealtimeUpdate("ORDER_STATUS_CHANGE", update);
    }

    /**
     * Send new order notification
     */
    public void sendNewOrderNotification(Long orderId, String clientName, int itemCount) {
        Map<String, Object> update = Map.of(
                "orderId", orderId,
                "clientName", clientName,
                "itemCount", itemCount,
                "timestamp", LocalDateTime.now()
        );

        sendRealtimeUpdate("NEW_ORDER", update);
    }

    /**
     * Send AI automation alert
     */
    public void sendAIAlert(String agentName, String action, Object details) {
        Map<String, Object> alert = Map.of(
                "agentName", agentName,
                "action", action,
                "details", details,
                "timestamp", LocalDateTime.now()
        );

        sendRealtimeUpdate("AI_ALERT", alert);
    }

    /**
     * Send statistics update
     */
    public void sendStatsUpdate(Map<String, Object> stats) {
        sendRealtimeUpdate("STATS_UPDATE", stats);
    }

    /**
     * Enhanced notification with WebSocket broadcast
     */
    @Override
    public void sendNotification(String clientId, String message) {
        // Original console notification
        super.sendNotification(clientId, message);

        // Real-time dashboard notification
        Map<String, Object> notification = Map.of(
                "clientId", clientId,
                "message", message,
                "type", "CLIENT_NOTIFICATION",
                "timestamp", LocalDateTime.now()
        );

        sendRealtimeUpdate("NOTIFICATION", notification);
    }

    /**
     * Enhanced internal notification with WebSocket broadcast
     */
    @Override
    public void sendInternalNotification(String department, String message) {
        // Original console notification
        super.sendInternalNotification(department, message);

        // Real-time dashboard notification
        Map<String, Object> notification = Map.of(
                "department", department,
                "message", message,
                "type", "INTERNAL_NOTIFICATION",
                "timestamp", LocalDateTime.now()
        );

        sendRealtimeUpdate("INTERNAL_NOTIFICATION", notification);
    }

    /**
     * Enhanced urgent alert with WebSocket broadcast
     */
    @Override
    public void sendUrgentAlert(String recipient, String alertMessage) {
        // Original console alert
        super.sendUrgentAlert(recipient, alertMessage);

        // Real-time dashboard alert
        Map<String, Object> alert = Map.of(
                "recipient", recipient,
                "message", alertMessage,
                "type", "URGENT_ALERT",
                "timestamp", LocalDateTime.now(),
                "severity", "HIGH"
        );

        sendRealtimeUpdate("URGENT_ALERT", alert);
    }
}