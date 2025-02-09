package io.abc.hrms.payroll.model.datacom;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class DatacomLeave {
    private String employeeNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double hours;
    private String notes;
    private LeaveType leaveType;
} 