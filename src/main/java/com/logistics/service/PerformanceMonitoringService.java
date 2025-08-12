package com.logistics.service;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for monitoring system performance during bulk testing
 */
@Service
public class PerformanceMonitoringService {

    private final AtomicLong totalOrdersProcessed = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalProcessingTimeMs = new AtomicLong(0);

    private final ConcurrentHashMap<String, Long> ordersByStatus = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Double> averageProcessingTimes = new ConcurrentHashMap<>();

    private LocalDateTime testStartTime;
    private boolean monitoringActive = false;

    /**
     * Start performance monitoring for bulk testing
     */
    public void startMonitoring() {
        System.out.println("Starting performance monitoring...");
        testStartTime = LocalDateTime.now();
        monitoringActive = true;

        // Reset counters
        totalOrdersProcessed.set(0);
        totalErrors.set(0);
        totalProcessingTimeMs.set(0);
        ordersByStatus.clear();
        averageProcessingTimes.clear();
    }

    /**
     * Stop performance monitoring
     */
    public void stopMonitoring() {
        monitoringActive = false;
        System.out.println("Performance monitoring stopped");
        printFinalReport();
    }

    /**
     * Record order processing metrics
     */
    public void recordOrderProcessed(String status, long processingTimeMs) {
        if (!monitoringActive) return;

        totalOrdersProcessed.incrementAndGet();
        totalProcessingTimeMs.addAndGet(processingTimeMs);
        ordersByStatus.merge(status, 1L, Long::sum);

        // Update average processing time for status
        String timeKey = status + "_processing_time";
        averageProcessingTimes.merge(timeKey, (double) processingTimeMs,
                (existing, newValue) -> (existing + newValue) / 2);
    }

    /**
     * Record error
     */
    public void recordError(String errorType) {
        if (!monitoringActive) return;
        totalErrors.incrementAndGet();
        ordersByStatus.merge("ERROR_" + errorType, 1L, Long::sum);
    }

    /**
     * Get current performance metrics
     */
    public Map<String, Object> getCurrentMetrics() {
        if (!monitoringActive) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "MONITORING_INACTIVE");
            return result;
        }

        long totalOrders = totalOrdersProcessed.get();
        long totalTime = totalProcessingTimeMs.get();
        long errors = totalErrors.get();

        double avgProcessingTime = totalOrders > 0 ? (double) totalTime / totalOrders : 0;
        double errorRate = totalOrders > 0 ? (double) errors / totalOrders * 100 : 0;

        // Calculate throughput
        long elapsedSeconds = testStartTime != null ?
                java.time.Duration.between(testStartTime, LocalDateTime.now()).toSeconds() : 1;
        double throughput = elapsedSeconds > 0 ? (double) totalOrders / elapsedSeconds : 0;

        // Get system memory info
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
        long freeMemory = runtime.freeMemory() / 1024 / 1024; // MB
        long usedMemory = totalMemory - freeMemory;
        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;

        Map<String, Object> systemMetrics = new HashMap<>();
        systemMetrics.put("usedMemory", usedMemory + " MB");
        systemMetrics.put("totalMemory", totalMemory + " MB");
        systemMetrics.put("memoryUsage", String.format("%.1f%%", memoryUsagePercent));
        systemMetrics.put("availableProcessors", runtime.availableProcessors());

        Map<String, Object> result = new HashMap<>();
        result.put("totalOrdersProcessed", totalOrders);
        result.put("totalErrors", errors);
        result.put("errorRate", String.format("%.2f%%", errorRate));
        result.put("averageProcessingTime", String.format("%.2f ms", avgProcessingTime));
        result.put("throughput", String.format("%.2f orders/sec", throughput));
        result.put("elapsedTime", elapsedSeconds + " seconds");
        result.put("ordersByStatus", new ConcurrentHashMap<>(ordersByStatus));
        result.put("systemMetrics", systemMetrics);
        result.put("testStartTime", testStartTime);
        result.put("status", "ACTIVE");

        return result;
    }

    /**
     * Print performance report every 30 seconds during monitoring
     */
    @Scheduled(fixedRate = 30000)
    public void printPerformanceReport() {
        if (!monitoringActive) return;

        Map<String, Object> metrics = getCurrentMetrics();

        System.out.println("\n===== PERFORMANCE REPORT =====");
        System.out.println("Elapsed Time: " + metrics.get("elapsedTime"));
        System.out.println("Total Orders: " + metrics.get("totalOrdersProcessed"));
        System.out.println("Throughput: " + metrics.get("throughput"));
        System.out.println("Avg Processing: " + metrics.get("averageProcessingTime"));
        System.out.println("Error Rate: " + metrics.get("errorRate"));

        @SuppressWarnings("unchecked")
        Map<String, Object> systemMetrics = (Map<String, Object>) metrics.get("systemMetrics");
        System.out.println("Memory Usage: " + systemMetrics.get("memoryUsage"));
        System.out.println("Orders by Status: " + metrics.get("ordersByStatus"));
        System.out.println("================================\n");
    }

    private void printFinalReport() {
        Map<String, Object> finalMetrics = getCurrentMetrics();

        System.out.println("\n===== FINAL PERFORMANCE REPORT =====");
        System.out.println("Test Duration: " + finalMetrics.get("elapsedTime"));
        System.out.println("Total Orders Processed: " + finalMetrics.get("totalOrdersProcessed"));
        System.out.println("Average Throughput: " + finalMetrics.get("throughput"));
        System.out.println("Average Processing Time: " + finalMetrics.get("averageProcessingTime"));
        System.out.println("Total Errors: " + finalMetrics.get("totalErrors"));
        System.out.println("Error Rate: " + finalMetrics.get("errorRate"));
        System.out.println("Final Status Distribution: " + finalMetrics.get("ordersByStatus"));

        @SuppressWarnings("unchecked")
        Map<String, Object> systemMetrics = (Map<String, Object>) finalMetrics.get("systemMetrics");
        System.out.println("Peak Memory Usage: " + systemMetrics.get("memoryUsage"));
        System.out.println("=====================================\n");
    }

    /**
     * Get performance recommendations based on current metrics
     */
    public Map<String, Object> getPerformanceRecommendations() {
        Map<String, Object> metrics = getCurrentMetrics();

        long totalOrders = (Long) metrics.get("totalOrdersProcessed");
        double errorRate = Double.parseDouble(((String) metrics.get("errorRate")).replace("%", ""));
        String throughputStr = (String) metrics.get("throughput");
        double throughput = Double.parseDouble(throughputStr.split(" ")[0]);

        @SuppressWarnings("unchecked")
        Map<String, Object> systemMetrics = (Map<String, Object>) metrics.get("systemMetrics");
        double memoryUsage = Double.parseDouble(((String) systemMetrics.get("memoryUsage")).replace("%", ""));

        // Generate recommendations
        List<String> recommendations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (errorRate > 10) {
            warnings.add("High error rate (" + errorRate + "%) - Check system stability");
            recommendations.add("Reduce order generation rate");
            recommendations.add("Check database connection pool settings");
        } else if (errorRate > 5) {
            warnings.add("Moderate error rate (" + errorRate + "%) - Monitor closely");
        }

        if (memoryUsage > 85) {
            warnings.add("High memory usage (" + memoryUsage + "%) - Risk of OutOfMemoryError");
            recommendations.add("Increase JVM heap size (-Xmx)");
            recommendations.add("Reduce concurrent order processing");
        } else if (memoryUsage > 70) {
            warnings.add("Elevated memory usage (" + memoryUsage + "%) - Monitor memory");
        }

        if (throughput < 1) {
            warnings.add("Low throughput (" + throughput + " orders/sec) - Performance issues detected");
            recommendations.add("Check database performance");
            recommendations.add("Optimize AI agent processing");
        }

        if (totalOrders > 1000 && errorRate < 2 && throughput > 5) {
            recommendations.add("Excellent performance! System can handle high loads");
            recommendations.add("Consider this configuration for production");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("currentMetrics", metrics);
        result.put("warnings", warnings);
        result.put("recommendations", recommendations);
        result.put("overallStatus", determineOverallStatus(errorRate, memoryUsage, throughput));
        result.put("nextSteps", getNextSteps(totalOrders, errorRate, throughput));

        return result;
    }

    private String determineOverallStatus(double errorRate, double memoryUsage, double throughput) {
        if (errorRate > 10 || memoryUsage > 90) {
            return "CRITICAL - Stop testing and investigate";
        } else if (errorRate > 5 || memoryUsage > 80 || throughput < 2) {
            return "WARNING - Monitor closely";
        } else if (throughput > 5 && errorRate < 2) {
            return "EXCELLENT - System performing well";
        } else {
            return "GOOD - Normal performance";
        }
    }

    private List<String> getNextSteps(long totalOrders, double errorRate, double throughput) {
        List<String> steps = new ArrayList<>();

        if (totalOrders < 100) {
            steps.add("Continue testing with larger order batches");
            steps.add("Try 'medium' intensity testing (200-500 orders)");
        } else if (totalOrders < 500 && errorRate < 5) {
            steps.add("System handling load well - try 'heavy' testing");
            steps.add("Test concurrent user scenarios");
        } else if (totalOrders > 500 && errorRate < 2) {
            steps.add("Excellent! Try 'stress' testing to find limits");
            steps.add("Consider production deployment with these settings");
        } else {
            steps.add("Optimize current configuration before increasing load");
            steps.add("Focus on reducing error rate and improving throughput");
        }

        return steps;
    }
}