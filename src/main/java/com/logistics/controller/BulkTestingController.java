package com.logistics.controller;

import com.logistics.service.BulkTestingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for bulk testing and performance evaluation
 */
@RestController
@RequestMapping("/api/testing")
@Tag(name = "Bulk Testing", description = "Real-world scale testing and performance evaluation")
public class BulkTestingController {

    @Autowired
    private BulkTestingService bulkTestingService;

    /**
     * Generate bulk orders for testing
     */
    @PostMapping("/bulk-orders")
    @Operation(summary = "Generate Bulk Orders",
            description = "Generate large numbers of orders for testing system performance")
    public ResponseEntity<Map<String, Object>> generateBulkOrders(
            @Parameter(description = "Number of orders to generate") @RequestParam(defaultValue = "100") int count,
            @Parameter(description = "Load intensity: light, medium, heavy, stress") @RequestParam(defaultValue = "medium") String intensity) {

        LocalDateTime startTime = LocalDateTime.now();

        try {
            // Run bulk generation asynchronously
            CompletableFuture.runAsync(() -> {
                bulkTestingService.generateBulkOrders(count, intensity);
            });

            Map<String, Object> response = Map.of(
                    "message", "Bulk order generation started",
                    "orderCount", count,
                    "intensity", intensity,
                    "startTime", startTime,
                    "estimatedDuration", calculateEstimatedDuration(count, intensity),
                    "status", "IN_PROGRESS"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "error", "Failed to start bulk order generation",
                    "message", e.getMessage(),
                    "status", "FAILED"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Quick performance test scenarios
     */
    @PostMapping("/scenarios/{scenario}")
    @Operation(summary = "Run Test Scenarios",
            description = "Run predefined test scenarios for different use cases")
    public ResponseEntity<Map<String, Object>> runTestScenario(
            @Parameter(description = "Scenario name") @PathVariable String scenario) {

        try {
            switch (scenario.toLowerCase()) {
                case "morning-rush":
                    return runMorningRushScenario();
                case "peak-season":
                    return runPeakSeasonScenario();
                case "system-stress":
                    return runSystemStressScenario();
                case "realistic-day":
                    return runRealisticDayScenario();
                default:
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Unknown scenario",
                            "availableScenarios", new String[]{"morning-rush", "peak-season", "system-stress", "realistic-day"}
                    ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to run scenario",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get testing recommendations based on system resources
     */
    @GetMapping("/recommendations")
    @Operation(summary = "Get Testing Recommendations",
            description = "Get recommended test parameters based on current system status")
    public ResponseEntity<Map<String, Object>> getTestingRecommendations() {

        // Simple resource-based recommendations
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024; // MB
        int processors = runtime.availableProcessors();

        Map<String, Object> recommendations = Map.of(
                "systemInfo", Map.of(
                        "maxMemory", maxMemory + " MB",
                        "processors", processors,
                        "recommendedConcurrency", Math.min(processors * 2, 10)
                ),
                "testScenarios", Map.of(
                        "light", Map.of(
                                "orders", 50,
                                "description", "50 orders, small load, good for initial testing"
                        ),
                        "medium", Map.of(
                                "orders", 200,
                                "description", "200 orders, realistic load, standard performance test"
                        ),
                        "heavy", Map.of(
                                "orders", 500,
                                "description", "500 orders, high load, stress testing"
                        ),
                        "stress", Map.of(
                                "orders", 1000,
                                "description", "1000+ orders, maximum load, system limits testing"
                        )
                ),
                "recommendations", Map.of(
                        "startWith", "light",
                        "incrementBy", 100,
                        "monitorMetrics", new String[]{"Memory usage", "Response times", "Error rates", "Database connections"},
                        "warningThresholds", Map.of(
                                "memoryUsage", "80%",
                                "responseTime", "5 seconds",
                                "errorRate", "5%"
                        )
                )
        );

        return ResponseEntity.ok(recommendations);
    }

    // Private scenario methods
    private ResponseEntity<Map<String, Object>> runMorningRushScenario() {
        CompletableFuture.runAsync(() -> {
            // Simulate morning rush: lots of small orders
            bulkTestingService.generateBulkOrders(150, "light");
        });

        return ResponseEntity.ok(Map.of(
                "scenario", "morning-rush",
                "description", "Simulating morning rush with 150 small orders",
                "orders", 150,
                "pattern", "Small orders from various clients",
                "status", "STARTED"
        ));
    }

    private ResponseEntity<Map<String, Object>> runPeakSeasonScenario() {
        CompletableFuture.runAsync(() -> {
            // Peak season: mix of order sizes, higher volume
            bulkTestingService.generateBulkOrders(300, "heavy");
        });

        return ResponseEntity.ok(Map.of(
                "scenario", "peak-season",
                "description", "Simulating peak construction season with mixed order sizes",
                "orders", 300,
                "pattern", "Mixed order sizes, high volume",
                "status", "STARTED"
        ));
    }

    private ResponseEntity<Map<String, Object>> runSystemStressScenario() {
        CompletableFuture.runAsync(() -> {
            // System stress: maximum concurrent load
            bulkTestingService.generateBulkOrders(1000, "stress");
        });

        return ResponseEntity.ok(Map.of(
                "scenario", "system-stress",
                "description", "Maximum load stress test with 1000 concurrent orders",
                "orders", 1000,
                "pattern", "Concurrent submission, maximum load",
                "status", "STARTED"
        ));
    }

    private ResponseEntity<Map<String, Object>> runRealisticDayScenario() {
        CompletableFuture.runAsync(() -> {
            // Realistic business day: gradual order flow
            bulkTestingService.generateBulkOrders(250, "medium");
        });

        return ResponseEntity.ok(Map.of(
                "scenario", "realistic-day",
                "description", "Simulating realistic business day order flow",
                "orders", 250,
                "pattern", "Gradual order submission throughout day",
                "status", "STARTED"
        ));
    }

    private String calculateEstimatedDuration(int count, String intensity) {
        // Rough estimates based on intensity
        double ordersPerSecond;
        switch (intensity.toLowerCase()) {
            case "light": ordersPerSecond = 2.0; break;
            case "medium": ordersPerSecond = 5.0; break;
            case "heavy": ordersPerSecond = 8.0; break;
            case "stress": ordersPerSecond = 20.0; break;
            default: ordersPerSecond = 5.0;
        }

        int estimatedSeconds = (int) (count / ordersPerSecond);
        return estimatedSeconds > 60 ?
                (estimatedSeconds / 60) + " minutes" :
                estimatedSeconds + " seconds";
    }
}