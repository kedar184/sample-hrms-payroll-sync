package io.abc.hrms.payroll.mock;

import io.abc.hrms.payroll.model.AbsenceType;
import io.abc.hrms.payroll.model.Employee;
import io.abc.hrms.payroll.model.Absence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/mock/hr")
@ConditionalOnProperty(name = "hr.api.mock.enabled", havingValue = "true")
public class MockHRController {
    
    private static final String MOCK_API_KEY = "mock-hr-api-key";
    
    private final Map<String, Employee> employees = new HashMap<>();
    private final Map<String, Absence> absences = new HashMap<>();
    
    @GetMapping("/employees/{id}")
    public ResponseEntity<Employee> getEmployee(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable String id) {
        if (!MOCK_API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Mock HR System - Fetching employee: {}", id);
        return employees.containsKey(id) 
            ? ResponseEntity.ok(employees.get(id))
            : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/absences/{id}")
    public ResponseEntity<Absence> getAbsence(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable String id) {
        if (!MOCK_API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Mock HR System - Fetching absence: {}", id);
        return absences.containsKey(id)
            ? ResponseEntity.ok(absences.get(id))
            : ResponseEntity.notFound().build();
    }
    
    @PostConstruct
    public void init() {
        // Create mock employees
        Employee emp1 = new Employee();
        emp1.setEmployeeId("EMP001");
        emp1.setFirstName("John");
        emp1.setLastName("Doe");
        emp1.setTaxNumber("NZ123456");
        emp1.setBankAccount("01-1234-1234567-00");
        emp1.setStartDate(LocalDate.now().minusYears(1));
        emp1.setPayGroup("WEEKLY");
        emp1.setHourlyRate(25.0);
        emp1.setRegion("NZ");
        employees.put(emp1.getEmployeeId(), emp1);

        Employee emp2 = new Employee();
        emp2.setEmployeeId("EMP002");
        emp2.setFirstName("Jane");
        emp2.setLastName("Smith");
        emp2.setTaxNumber("NZ789012");
        emp2.setBankAccount("01-5678-1234567-00");
        emp2.setStartDate(LocalDate.now().minusMonths(6));
        emp2.setPayGroup("WEEKLY");
        emp2.setHourlyRate(27.0);
        emp2.setRegion("NZ");
        employees.put(emp2.getEmployeeId(), emp2);

        // Create mock absences
        Absence abs1 = new Absence();
        abs1.setEmployee(emp1);
        abs1.setStartDate(LocalDate.now().plusDays(5));
        abs1.setEndDate(LocalDate.now().plusDays(10));
        abs1.setHours(40.0);
        abs1.setType(AbsenceType.ANNUAL_LEAVE);
        absences.put("ABS001", abs1);

        Absence abs2 = new Absence();
        abs2.setEmployee(emp2);
        abs2.setStartDate(LocalDate.now());
        abs2.setEndDate(LocalDate.now().plusDays(2));
        abs2.setHours(16.0);
        abs2.setType(AbsenceType.SICK_LEAVE);
        absences.put("ABS002", abs2);
    }
} 