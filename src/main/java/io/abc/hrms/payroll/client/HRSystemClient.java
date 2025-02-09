package io.abc.hrms.payroll.client;

import io.abc.hrms.payroll.model.Employee;
import io.abc.hrms.payroll.model.Absence;
import io.abc.hrms.payroll.service.SecretService;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class HRSystemClient {
    
    private final RestTemplate restTemplate;
    private final SecretService secretService;
    
    private static final String API_KEY_SECRET = "secrets/integration/services/hr-system-api-key";
    
    @Value("${hr.api.baseUrl}")
    private String baseUrl;
    
    @Value("${hr.api.mock.enabled:false}")
    private boolean mockEnabled;
    
    private String getBaseUrl() {
        return mockEnabled ? "http://localhost:8080" : baseUrl;
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (!mockEnabled) {
            headers.set("X-API-Key", secretService.getSecret(API_KEY_SECRET));
        }
        return headers;
    }
    
    @Retry(name = "hrApi")
    @CircuitBreaker(name = "hrApi")
    public Employee getEmployee(String employeeId) {
        HttpEntity<?> request = new HttpEntity<>(createHeaders());
        return restTemplate.exchange(
            getBaseUrl() + "/api/employees/{id}",
            HttpMethod.GET,
            request,
            Employee.class,
            employeeId
        ).getBody();
    }
    
    @Retry(name = "hrApi")
    @CircuitBreaker(name = "hrApi")
    public Absence getAbsence(String absenceId) {
        HttpEntity<?> request = new HttpEntity<>(createHeaders());
        return restTemplate.exchange(
            getBaseUrl() + "/api/absences/{id}",
            HttpMethod.GET,
            request,
            Absence.class,
            absenceId
        ).getBody();
    }
} 