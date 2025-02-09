package io.abc.hrms.payroll.service;

import io.abc.hrms.payroll.service.DatacomAuthService.TokenResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatacomAuthServiceTest {

    @Mock
    private SecretService secretService;
    
    @Mock
    private RestTemplate restTemplate;
    
    @InjectMocks
    private DatacomAuthService authService;
    
    @Test
    void shouldReturnCachedTokenWhenValid() {
        // Arrange
        when(secretService.getSecret(any())).thenReturn("test-secret");
        when(restTemplate.postForObject(
            any(), any(HttpEntity.class), eq(TokenResponse.class)
        )).thenReturn(new TokenResponse("token123", 3600, "Bearer"));
        
        // Act
        String token1 = authService.getValidToken();
        String token2 = authService.getValidToken();
        
        // Assert
        assertEquals(token1, token2);
        verify(restTemplate, times(1)).postForObject(
            any(), any(HttpEntity.class), eq(TokenResponse.class)
        );
    }
    
    @Test
    void shouldFetchNewTokenWhenExpired() throws InterruptedException {
        // Arrange
        when(secretService.getSecret(any())).thenReturn("test-secret");
        when(restTemplate.postForObject(
            any(), any(HttpEntity.class), eq(TokenResponse.class)
        )).thenReturn(
            new TokenResponse("token1", 1, "Bearer"),
            new TokenResponse("token2", 3600, "Bearer")
        );
        
        // Act
        String token1 = authService.getValidToken();
        Thread.sleep(2000); // Wait for token to expire
        String token2 = authService.getValidToken();
        
        // Assert
        assertNotEquals(token1, token2);
        verify(restTemplate, times(2)).postForObject(
            any(), any(HttpEntity.class), eq(TokenResponse.class)
        );
    }

    @Test
    void shouldThrowExceptionWhenSecretServiceReturnsNull() {
        // Arrange
        when(secretService.getSecret(any())).thenReturn(null);
        
        // Act & Assert
        assertThrows(HttpClientErrorException.class, () -> {
            authService.getValidToken();
        });
    }
} 