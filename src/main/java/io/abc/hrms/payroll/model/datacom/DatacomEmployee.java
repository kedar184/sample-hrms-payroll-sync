package io.abc.hrms.payroll.model.datacom;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatacomEmployee {
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String region;
    private String taxNumber;
    private String bankAccount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String payGroup;
    private Double hourlyRate;
    private EmploymentType employmentType;
    private TaxCode taxCode;
    private KiwiSaverStatus kiwiSaverStatus;
    private Double kiwiSaverRate;
} 