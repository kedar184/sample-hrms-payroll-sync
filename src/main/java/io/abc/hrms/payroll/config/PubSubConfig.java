package io.abc.hrms.payroll.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")  // Only active in non-local profiles
public class PubSubConfig {
    // Remove manual bean creation - Spring Boot will auto-configure PubSubTemplate
} 