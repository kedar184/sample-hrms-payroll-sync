package io.abc.hrms.payroll.model.notification;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class BusinessNotification {
    private String serviceName;
    private String eventId;
    private String recordType;
    private String recordId;
    private String errorCode;
    private String errorMessage;
    private String details;
    private Instant timestamp;
    private NotificationSeverity severity;
    
    public enum NotificationSeverity {
        INFO, WARNING, ERROR
    }
} 