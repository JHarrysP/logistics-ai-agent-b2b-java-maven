package com.logistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
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

    private static final Logger log = LoggerFactory.getLogger(LogisticsAIAgentApplication.class);

    public static void main(String[] args) {
        // Add JVM shutdown hook before starting application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM shutdown hook executed - Application terminating gracefully");
        }));

        try {
            // Configure SpringApplication for proper shutdown handling
            SpringApplication app = new SpringApplication(LogisticsAIAgentApplication.class);
            app.setRegisterShutdownHook(true);

            // Add application event listeners for enhanced shutdown handling
            app.addListeners(new ApplicationListener<ContextClosedEvent>() {
                @Override
                public void onApplicationEvent(ContextClosedEvent event) {
                    log.info("Spring context closing - initiating graceful shutdown");
                }
            });

            ConfigurableApplicationContext context = app.run(args);
            Environment env = context.getEnvironment();

            String protocol = "http";
            if (env.getProperty("server.ssl.key-store") != null) {
                protocol = "https";
            }

            String serverPort = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "");

            log.info("Application 'Logistics AI Agent' is running! Access URLs:");
            log.info("Local: {}://localhost:{}{}", protocol, serverPort, contextPath);
            log.info("API Documentation: {}://localhost:{}{}/swagger-ui.html", protocol, serverPort, contextPath);
            log.info("H2 Console: {}://localhost:{}{}/h2-console", protocol, serverPort, contextPath);
            log.info("Actuator Health: {}://localhost:{}{}/actuator/health", protocol, serverPort, contextPath);
            log.info("Test Endpoints: {}://localhost:{}{}/test/status", protocol, serverPort, contextPath);
            log.info("Profile(s): {}", java.util.Arrays.toString(env.getActiveProfiles()));

        } catch (Exception e) {
            log.error("Failed to start Logistics AI Agent application", e);
            System.exit(1);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Logistics AI Agent Application started successfully");

        // Log runtime information for debugging
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;

        log.info("JVM Memory Status - Used: {}MB, Free: {}MB, Total: {}MB, Max: {}MB",
                usedMemory, freeMemory, totalMemory, maxMemory);

        if (usedMemory > maxMemory * 0.8) {
            log.warn("WARNING: High memory usage detected at startup ({}%)",
                    (usedMemory * 100) / maxMemory);
        }
    }

    @Bean
    public ApplicationListener<ContextClosedEvent> shutdownListener() {
        return event -> {
            log.info("Application shutdown initiated - Context: {}",
                    event.getApplicationContext().getDisplayName());

            try {
                // Give running tasks time to complete
                log.info("Waiting for running tasks to complete...");
                Thread.sleep(3000);
                log.info("Graceful shutdown cleanup completed successfully");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Shutdown process interrupted - forcing immediate shutdown");
            }
        };
    }
}