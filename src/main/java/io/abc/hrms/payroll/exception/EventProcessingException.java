package io.abc.hrms.payroll.exception;

import lombok.Getter;

@Getter
public class EventProcessingException extends RuntimeException {
    private final int statusCode;
    
    public EventProcessingException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
} 