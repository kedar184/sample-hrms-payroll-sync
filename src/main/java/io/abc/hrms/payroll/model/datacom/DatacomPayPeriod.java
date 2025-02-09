package io.abc.hrms.payroll.model.datacom;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DatacomPayPeriod {
    private LocalDate startDate;
    private LocalDate endDate;
    private String frequency;
    private String status;
} 