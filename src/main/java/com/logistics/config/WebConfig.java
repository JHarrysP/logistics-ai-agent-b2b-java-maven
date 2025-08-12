package com.logistics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for CORS and other web-related settings
 */
@Configuration
@SuppressWarnings("unused")
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configure CORS for API endpoints with specific origins
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000", 
                    "http://localhost:8080", 
                    "https://logistics.company.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("Authorization", "Content-Type", "Accept", "X-Requested-With")
                .allowCredentials(true)
                .maxAge(3600);

        // More restrictive CORS for actuator endpoints (only health check publicly accessible)
        registry.addMapping("/actuator/health")
                .allowedOrigins("https://logistics.company.com")
                .allowedMethods("GET")
                .allowedHeaders("Content-Type")
                .allowCredentials(false)
                .maxAge(300);
    }
}