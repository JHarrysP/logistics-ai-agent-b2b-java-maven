package com.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Application Entry Point for B2B Logistics AI Agent
 * 
 * This Spring Boot application provides AI-powered workflow automation
 * for B2B logistics operations including:
 * - Order processing and validation
 * - Inventory management
 * - Warehouse operations
 * - Shipping coordination
 * 
 * @author Logistics Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class LogisticsAIAgentApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LogisticsAIAgentApplication.class, args);
        System.out.println("Logistics AI Agent started successfully!");
        System.out.println("Access API documentation at: http://localhost:8080/swagger-ui.html");
        System.out.println("Access H2 Console at: http://localhost:8080/h2-console");
        System.out.println("Health check at: http://localhost:8080/actuator/health");
    }
}