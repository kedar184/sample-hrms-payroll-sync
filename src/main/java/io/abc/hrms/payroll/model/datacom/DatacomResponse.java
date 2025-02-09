package io.abc.hrms.payroll.model.datacom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatacomResponse {
    private boolean success;
    private String id;
    private String message;
    private String errorCode;
    
    public DatacomResponse(boolean success, String id, String message) {
        this(success, id, message, null);
    }

    // Explicit getters to match method calls
    public boolean success() {
        return success;
    }

    public String message() {
        return message;
    }

    public String errorCode() {
        return errorCode;
    }
} 