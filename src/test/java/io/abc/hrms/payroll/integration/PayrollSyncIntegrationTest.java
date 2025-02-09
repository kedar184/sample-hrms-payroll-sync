package io.abc.hrms.payroll.integration;

import io.abc.hrms.payroll.model.event.HREvent;
import io.abc.hrms.payroll.service.PayrollSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class PayrollSyncIntegrationTest {

    @Autowired
    private PayrollSyncService payrollSyncService;

    @Test
    void shouldHandleEmployeeEventEndToEnd() {
        // Arrange
        HREvent event = new HREvent();
        event.setEventType("EMPLOYEE_CREATED");
        event.setRecordType("EMPLOYEE");
        event.setRecordId("EMP001");

        // Act & Assert
        assertDoesNotThrow(() -> payrollSyncService.processEmployeeEvent(event));
    }
} 