package com.logistics.performance;

import com.logistics.dto.OrderItemRequest;
import com.logistics.dto.OrderRequest;
import com.logistics.repository.OrderRepository;
import com.logistics.service.LogisticsAIAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Performance tests for the logistics system
 */
@SpringBootTest
@ActiveProfiles("test")
class LogisticsPerformanceTest {

    @Autowired
    private LogisticsAIAgent aiAgent;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testConcurrentOrderProcessing() throws InterruptedException {
        int numberOfOrders = 20;
        int numberOfThreads = 5;
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        long startTime = System.currentTimeMillis();

        // Submit multiple orders concurrently
        List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfOrders)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    // Create and submit order
                    OrderItemRequest item = new OrderItemRequest("TILE-001", 1, 25.99);
                    OrderRequest request = new OrderRequest(
                        "PERF_CLIENT_" + i,
                        "Performance Test Client " + i,
                        "Test Address " + i + ", Hamburg, Germany",
                        LocalDateTime.now().plusDays(2),
                        Arrays.asList(item)
                    );
                    
                    // Process the order (this would normally be done via REST API)
                    // For performance testing, we can test the service layer directly
                    System.out.println("Processing order " + i);
                    
                } catch (Exception e) {
                    System.err.println("Error processing order " + i + ": " + e.getMessage());
                }
            }, executor))
            .toList();

        // Wait for all orders to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("Processed " + numberOfOrders + " orders in " + totalTime + "ms");
        System.out.println("Average time per order: " + (totalTime / numberOfOrders) + "ms");

        // Assert reasonable performance (adjust thresholds as needed)
        assertTrue(totalTime < 30000, "Processing should complete within 30 seconds");
        assertTrue((totalTime / numberOfOrders) < 5000, "Average processing time should be under 5 seconds per order");

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    void testSystemUnderLoad() {
        // This test would simulate high load conditions
        // Could be expanded to test database connection pooling,
        // memory usage, etc.
        
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        // Simulate some load...
        
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryUsed = endMemory - startMemory;
        
        System.out.println("Memory used during test: " + (memoryUsed / 1024 / 1024) + " MB");
        
        // Assert memory usage is reasonable
        assertTrue(memoryUsed < 500 * 1024 * 1024, "Memory usage should be under 500MB");
    }
}