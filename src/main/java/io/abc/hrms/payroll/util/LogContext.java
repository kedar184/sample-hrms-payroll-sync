package io.abc.hrms.payroll.util;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

@UtilityClass
public class LogContext {
    public static final String RECORD_ID = "recordId";
    public static final String RECORD_TYPE = "recordType";
    public static final String EVENT_TYPE = "eventType";
    
    public static void setEventContext(String recordId, String recordType, String eventType) {
        MDC.put(RECORD_ID, recordId);
        MDC.put(RECORD_TYPE, recordType);
        MDC.put(EVENT_TYPE, eventType);
    }
    
    public static void clear() {
        MDC.clear();
    }
    
    public static void clearEventContext() {
        MDC.remove(RECORD_ID);
        MDC.remove(RECORD_TYPE);
        MDC.remove(EVENT_TYPE);
    }
} 