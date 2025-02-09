package io.abc.hrms.payroll.mock;

import io.abc.hrms.payroll.service.SecretService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@Profile("local")
@ConditionalOnProperty(name = {"hr.api.mock.enabled", "datacom.api.mock.enabled"}, havingValue = "true")
public class LocalSecretConfig {
    
    @Bean
    public SecretService secretService() {
        return new SecretService(null) {
            @Override
            public String getSecret(String secretName) {
                return switch (secretName) {
                    case "secrets/integration/services/datacom-client-id" -> "mock-client-id";
                    case "secrets/integration/services/datacom-client-secret" -> "mock-client-secret";
                    case "secrets/integration/services/hr-system-api-key" -> "mock-hr-api-key";
                    default -> null;
                };
            }
        };
    }
} 