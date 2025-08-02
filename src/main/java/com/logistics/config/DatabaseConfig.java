package com.logistics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database configuration
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.logistics.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
    
    // Database configuration is primarily handled through application.yml
    // This class can be extended for custom database beans if needed
}