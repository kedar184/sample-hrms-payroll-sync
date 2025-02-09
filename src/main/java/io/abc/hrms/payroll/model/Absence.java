package io.abc.hrms.payroll.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Absence {
    private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double hours;
    private String notes;
    private AbsenceType type;
} 