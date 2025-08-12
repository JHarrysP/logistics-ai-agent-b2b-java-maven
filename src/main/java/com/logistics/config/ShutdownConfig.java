package com.logistics.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PreDestroy;

@Configuration
public class ShutdownConfig {

    private static final Logger log = LoggerFactory.getLogger(ShutdownConfig.class);

    @Bean(name = "logisticsTaskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("logistics-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        log.info("Configured logistics task executor with graceful shutdown");
        return executor;
    }

    @Bean(name = "logisticsTaskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("logistics-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();

        log.info("Configured logistics task scheduler with graceful shutdown");
        return scheduler;
    }

    @PreDestroy
    public void onShutdown() {
        log.info("ShutdownConfig: Initiating graceful shutdown of task executors");

        try {
            // Additional cleanup logic can be added here
            Thread.sleep(1000); // Give tasks time to complete
            log.info("ShutdownConfig: Graceful shutdown completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("ShutdownConfig: Shutdown process interrupted");
        }
    }
}