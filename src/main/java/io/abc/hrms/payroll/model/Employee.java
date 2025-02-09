package io.abc.hrms.payroll.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Employee {
    private String employeeId;
    private String firstName;
    private String lastName;
    private String taxNumber;
    private String bankAccount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String payGroup;
    private Double hourlyRate;
    private String region;
    private EmploymentStatus status;
} 