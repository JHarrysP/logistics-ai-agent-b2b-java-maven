package com.logistics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test to ensure the application context loads correctly
 */
@SpringBootTest
@ActiveProfiles("test")
class LogisticsAIAgentApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
        // If there are any configuration issues, this test will fail
    }
}