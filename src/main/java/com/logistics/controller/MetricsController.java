package com.logistics.controller;

import com.logistics.service.PerformanceMonitoringService;
import com.logistics.service.LogisticsAIAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

/**
 * REST Controller for exposing system metrics and performance data
 */
@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "*")
public class MetricsController {

    @Autowired
    private PerformanceMonitoringService performanceMonitoringService;

    @Autowired
    private LogisticsAIAgent aiAgent;

    /**
     * Get current system metrics - This endpoint is called by the dashboard
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentMetrics() {
        try {
            Map<String, Object> metrics = performanceMonitoringService.getCurrentMetrics();

            // Add AI agent metrics if available
            try {
                Map<String, Object> aiMetrics = aiAgent.getAIMetrics();
                metrics.put("aiMetrics", aiMetrics);
            } catch (Exception e) {
                metrics.put("aiMetricsError", "Could not retrieve AI metrics: " + e.getMessage());
            }

            // Add system health status
            metrics.put("timestamp", System.currentTimeMillis());
            metrics.put("healthy", true);

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve metrics: " + e.getMessage());
            errorResponse.put("healthy", false);
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get performance recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations() {
        try {
            return ResponseEntity.ok(performanceMonitoringService.getPerformanceRecommendations());
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get recommendations: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Start performance monitoring
     */
    @PostMapping("/monitoring/start")
    public ResponseEntity<Map<String, String>> startMonitoring() {
        try {
            performanceMonitoringService.startMonitoring();
            Map<String, String> response = new HashMap<>();
            response.put("status", "Monitoring started");
            response.put("message", "Performance monitoring is now active");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to start monitoring: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Stop performance monitoring
     */
    @PostMapping("/monitoring/stop")
    public ResponseEntity<Map<String, String>> stopMonitoring() {
        try {
            performanceMonitoringService.stopMonitoring();
            Map<String, String> response = new HashMap<>();
            response.put("status", "Monitoring stopped");
            response.put("message", "Performance monitoring has been stopped");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to stop monitoring: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get system health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        try {
            Map<String, Object> health = new HashMap<>();

            // Check basic system stats
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory() / 1024 / 1024;
            long freeMemory = runtime.freeMemory() / 1024 / 1024;
            long usedMemory = totalMemory - freeMemory;
            double memoryUsage = (double) usedMemory / totalMemory * 100;

            health.put("status", memoryUsage < 90 ? "UP" : "WARNING");
            health.put("memoryUsage", String.format("%.1f%%", memoryUsage));
            health.put("totalMemory", totalMemory + " MB");
            health.put("usedMemory", usedMemory + " MB");
            health.put("availableProcessors", runtime.availableProcessors());
            health.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "DOWN");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
