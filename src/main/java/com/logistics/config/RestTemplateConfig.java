package com.logistics.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for RestTemplate bean to handle HTTP client operations
 * This resolves circular dependency issues by properly defining the RestTemplate bean
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a configured RestTemplate bean with appropriate timeouts
     * for reliable HTTP communication in the logistics system
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
}
