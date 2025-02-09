package io.abc.hrms.payroll.service;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cloud.gcp.secretmanager.enabled", havingValue = "true", matchIfMissing = true)
public class SecretService {
    
    private final SecretManagerServiceClient secretManagerClient;
    
    @Value("${spring.cloud.gcp.project-id}")
    private String projectId;
    
    public String getSecret(String secretName) {
        String fullSecretName = String.format(
            "projects/%s/secrets/%s/versions/latest",
            projectId,
            secretName
        );
        
        try {
            AccessSecretVersionResponse response = secretManagerClient.accessSecretVersion(
                SecretVersionName.parse(fullSecretName)
            );
            return response.getPayload().getData().toStringUtf8();
        } catch (Exception e) {
            log.error("Error retrieving secret: {}", secretName, e);
            throw new RuntimeException("Could not retrieve secret: " + secretName, e);
        }
    }
} 