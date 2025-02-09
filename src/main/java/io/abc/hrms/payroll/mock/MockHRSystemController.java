package io.abc.hrms.payroll.mock;

import io.abc.hrms.payroll.model.Employee;
import io.abc.hrms.payroll.model.Absence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@ConditionalOnProperty(name = "hr.api.mock.enabled", havingValue = "true")
public class MockHRSystemController {
    
    private final Map<String, Employee> employees = new HashMap<>();
    private final Map<String, Absence> absences = new HashMap<>();
    
    @GetMapping("/employees/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable String id) {
        log.info("Mock HR System - Fetching employee: {}", id);
        
        // Return mock employee if not found
        if (!employees.containsKey(id)) {
            Employee mockEmployee = new Employee();
            mockEmployee.setEmployeeId(id);
            mockEmployee.setFirstName("Test");
            mockEmployee.setLastName("User");
            mockEmployee.setRegion("NZ");
            return ResponseEntity.ok(mockEmployee);
        }
        
        return ResponseEntity.ok(employees.get(id));
    }
    
    @GetMapping("/absences/{id}")
    public ResponseEntity<Absence> getAbsence(@PathVariable String id) {
        log.info("Mock HR System - Fetching absence: {}", id);
        
        if (!absences.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(absences.get(id));
    }
} 