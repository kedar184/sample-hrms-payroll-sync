package io.abc.hrms.payroll.client;
 

import io.abc.hrms.payroll.model.datacom.*;
import io.abc.hrms.payroll.service.DatacomAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.abc.hrms.payroll.exception.DatacomApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatacomApiClient {
    
    private final RestTemplate restTemplate;
    private final DatacomAuthService authService;
    
    @Value("${datacom.api.baseUrl}")
    private String baseUrl;
    
    @Value("${datacom.api.mock.enabled:false}")
    private boolean mockEnabled;
    
    private String getBaseUrl() {
        return mockEnabled ? "http://localhost:8080/datacom/api" : baseUrl;
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (!mockEnabled) {
            headers.setBearerAuth(authService.getValidToken());
        }
        return headers;
    }
    
    @Retry(name = "datacomApi")
    @CircuitBreaker(name = "datacomApi")
    public DatacomResponse updateEmployee(DatacomEmployee payload) {
        log.debug("Sending employee update to Datacom: {}", payload);
        
        HttpEntity<DatacomEmployee> request = new HttpEntity<>(payload, createHeaders());
        DatacomResponse response = restTemplate.exchange(
            getBaseUrl() + "/employees",
            HttpMethod.POST,
            request,
            DatacomResponse.class
        ).getBody();
        
        log.debug("Received response from Datacom: {}", response);
        
        if (!response.success()) {
            throw new DatacomApiException(response.message(), response.errorCode());
        }
        
        return response;
    }
    
    @Retry(name = "datacomApi")
    @CircuitBreaker(name = "datacomApi")
    public DatacomResponse submitLeave(DatacomLeave payload) {
        HttpEntity<DatacomLeave> request = new HttpEntity<>(payload, createHeaders());
        return restTemplate.exchange(
            getBaseUrl() + "/leave",
            HttpMethod.POST,
            request,
            DatacomResponse.class
        ).getBody();
    }
    
    public DatacomPayPeriod getCurrentPayPeriod() {
        HttpEntity<?> request = new HttpEntity<>(createHeaders());
        return restTemplate.exchange(
            getBaseUrl() + "/pay-periods/current",
            HttpMethod.GET,
            request,
            DatacomPayPeriod.class
        ).getBody();
    }
} 