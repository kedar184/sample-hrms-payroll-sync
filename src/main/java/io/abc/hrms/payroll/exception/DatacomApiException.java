package io.abc.hrms.payroll.exception;

import lombok.Getter;

@Getter
public class DatacomApiException extends RuntimeException {
    private final String errorCode;
    
    public DatacomApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
} 