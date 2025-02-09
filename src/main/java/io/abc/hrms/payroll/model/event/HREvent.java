package io.abc.hrms.payroll.model.event;

import lombok.Data;

@Data
public class HREvent {
    private String eventId;      // Add this field
    private String eventType;     // EMPLOYEE_CHANGED, ABSENCE_CREATED, etc.
    private String recordType;    // EMPLOYEE, ABSENCE
    private String recordId;      // The ID to query HR system
    private String changeType;    // INSERT, UPDATE, DELETE
    private String timestamp;
} 