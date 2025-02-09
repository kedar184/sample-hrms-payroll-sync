package io.abc.hrms.payroll.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatacomAuthService {
    
    private final SecretService secretService;
    private final RestTemplate restTemplate;
    
    private static final String TOKEN_ENDPOINT = "https://api.datacomdirectaccess.co.nz/oauth2/token";
    private static final String CLIENT_ID_SECRET = "secrets/integration/services/datacom-client-id";
    private static final String CLIENT_SECRET_SECRET = "secrets/integration/services/datacom-client-secret";
    
    private final AtomicReference<TokenInfo> currentToken = new AtomicReference<>();
    
    public String getValidToken() {
        TokenInfo token = currentToken.get();
        
        if (token == null || token.isExpired()) {
            synchronized (this) {
                token = currentToken.get();
                if (token == null || token.isExpired()) {
                    token = fetchNewToken();
                    currentToken.set(token);
                }
            }
        }
        
        return token.accessToken();
    }
    
    private TokenInfo fetchNewToken() {
        String clientId = secretService.getSecret(CLIENT_ID_SECRET);
        String clientSecret = secretService.getSecret(CLIENT_SECRET_SECRET);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        
        try {
            TokenResponse response = restTemplate.postForObject(
                TOKEN_ENDPOINT,
                request,
                TokenResponse.class
            );
            
            if (response == null || response.access_token == null) {
                throw new RuntimeException("Invalid token response from Datacom");
            }
            
            return new TokenInfo(
                response.access_token,
                Instant.now().plusSeconds(response.expires_in)
            );
            
        } catch (Exception e) {
            log.error("Error fetching Datacom access token", e);
            throw new RuntimeException("Could not fetch Datacom access token", e);
        }
    }
    
    private record TokenInfo(String accessToken, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt.minusSeconds(60)); // 1 minute buffer
        }
    }
    
    public record TokenResponse(
        String access_token, 
        int expires_in, 
        String token_type, 
        String error
    ) {
        public TokenResponse(String access_token, int expires_in, String token_type) {
            this(access_token, expires_in, token_type, null);
        }
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    private void cleanupExpiredToken() {
        TokenInfo token = currentToken.get();
        if (token != null && token.isExpired()) {
            currentToken.set(null);
        }
    }
} 