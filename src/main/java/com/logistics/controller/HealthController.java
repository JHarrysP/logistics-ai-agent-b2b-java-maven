package com.logistics.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private DataSource dataSource;

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        Map<String, Object> response = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            response.put("status", "OK");
            response.put("connection", "Active");
            response.put("url", conn.getMetaData().getURL());
            response.put("driver", conn.getMetaData().getDriverName());

            log.info("Database connection test: SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());

            log.error("Database connection test: FAILED", e);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> testMemory() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memInfo = new HashMap<>();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;

        memInfo.put("totalMemoryMB", totalMemory / 1024 / 1024);
        memInfo.put("freeMemoryMB", freeMemory / 1024 / 1024);
        memInfo.put("maxMemoryMB", maxMemory / 1024 / 1024);
        memInfo.put("usedMemoryMB", usedMemory / 1024 / 1024);
        memInfo.put("memoryUtilization", String.format("%.2f%%", (double) usedMemory / maxMemory * 100));

        // Add warning if memory usage is high
        double utilizationPercent = (double) usedMemory / maxMemory * 100;
        if (utilizationPercent > 80) {
            memInfo.put("warning", "High memory usage detected");
        }

        log.info("Memory usage: {}MB/{}MB ({}%)",
                usedMemory / 1024 / 1024,
                maxMemory / 1024 / 1024,
                String.format("%.2f", utilizationPercent));

        return ResponseEntity.ok(memInfo);
    }

    @GetMapping("/threads")
    public ResponseEntity<Map<String, Object>> testThreads() {
        Map<String, Object> threadInfo = new HashMap<>();

        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }

        threadInfo.put("activeThreadCount", Thread.activeCount());
        threadInfo.put("totalThreadCount", rootGroup.activeCount());

        // Get scheduler-specific threads
        Thread[] threads = new Thread[rootGroup.activeCount()];
        rootGroup.enumerate(threads);

        long schedulerThreads = 0;
        long logisticsThreads = 0;

        for (Thread thread : threads) {
            if (thread != null) {
                String name = thread.getName();
                if (name.contains("logistics-scheduler")) {
                    schedulerThreads++;
                }
                if (name.contains("logistics")) {
                    logisticsThreads++;
                }
            }
        }

        threadInfo.put("schedulerThreads", schedulerThreads);
        threadInfo.put("logisticsThreads", logisticsThreads);

        return ResponseEntity.ok(threadInfo);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getOverallStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            // Quick database check
            try (Connection conn = dataSource.getConnection()) {
                status.put("database", "UP");
            }

            // Memory check
            Runtime runtime = Runtime.getRuntime();
            double memoryUsage = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory() * 100;
            status.put("memoryUsagePercent", String.format("%.2f", memoryUsage));
            status.put("memory", memoryUsage < 90 ? "UP" : "WARNING");

            // Overall status
            status.put("application", "UP");
            status.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            status.put("application", "DOWN");
            status.put("error", e.getMessage());
            return ResponseEntity.status(500).body(status);
        }
    }
}