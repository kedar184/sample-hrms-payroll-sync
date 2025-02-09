package io.abc.hrms.payroll.mock;

import io.abc.hrms.payroll.model.datacom.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/datacom/api")
@ConditionalOnProperty(name = "datacom.api.mock.enabled", havingValue = "true")
public class MockDatacomController {

    private final Map<String, DatacomEmployee> employees = new HashMap<>();
    private final Map<String, DatacomLeave> leaves = new HashMap<>();
    
    @PostMapping("/employees")
    public ResponseEntity<DatacomResponse> updateEmployee(@RequestBody DatacomEmployee employee) {
        // Simulate business validation error
        if ("INVALID_REGION".equals(employee.getRegion())) {
            return ResponseEntity.ok(new DatacomResponse(
                false, 
                employee.getEmployeeNumber(),
                "Invalid region code",
                "REGION_001"
            ));
        }
        
        log.info("Mock Datacom - Creating/updating employee: {}", employee);
        String id = employee.getEmployeeNumber();
        employees.put(id, employee);
        return ResponseEntity.ok(new DatacomResponse(true, id, "Employee processed successfully"));
    }

    @PostMapping("/leave")
    public ResponseEntity<DatacomResponse> submitLeave(@RequestBody DatacomLeave leave) {
        log.info("Mock Datacom - Submitting leave: {}", leave);
        String id = leave.getEmployeeNumber();
        leaves.put(id, leave);
        return ResponseEntity.ok(new DatacomResponse(true, id, "Leave processed successfully"));
    }

    @GetMapping("/pay-periods/current")
    public ResponseEntity<DatacomPayPeriod> getCurrentPayPeriod() {
        DatacomPayPeriod period = new DatacomPayPeriod(
            LocalDate.now().minusDays(7),
            LocalDate.now(),
            "WEEKLY",
            "OPEN"
        );
        return ResponseEntity.ok(period);
    }
} 