package com.example.order.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Test application configuration that disables method security.
 * This overrides the default OrderManagementApplication configuration during tests.
 */
@SpringBootConfiguration
@ComponentScan("com.example.order")
@EnableMethodSecurity(prePostEnabled = false)  // Disable prepost auth checks
@Profile("test")
public class TestApplication {
}
