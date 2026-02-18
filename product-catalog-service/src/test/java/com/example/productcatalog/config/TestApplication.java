package com.example.productcatalog.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Test application configuration that disables method security.
 * This overrides the default ProductCatalogApplication configuration during tests.
 */
@SpringBootConfiguration
@ComponentScan("com.example.productcatalog")
@EnableMethodSecurity(prePostEnabled = false)  // Disable prepost auth checks
@Profile("test")
public class TestApplication {
}
