package io.abc.hrms.payroll.mapper;

import io.abc.hrms.payroll.model.Employee;
import io.abc.hrms.payroll.model.Absence;
import io.abc.hrms.payroll.model.AbsenceType;
import io.abc.hrms.payroll.model.datacom.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatacomMapper {

    /**
     * Maps an HR employee to Datacom format.
     * Only processes NZ employees (tax number starts with NZ).
     * Sets default KiwiSaver and tax settings for new employees.
     */
    public DatacomEmployee toDatacomEmployee(Employee employee) {
        if (!isNZEmployee(employee)) {
            log.info("Skipping non-NZ employee: {}", employee.getEmployeeId());
            return null;
        }

        DatacomEmployee datacomEmployee = new DatacomEmployee();
        datacomEmployee.setEmployeeNumber(employee.getEmployeeId());
        datacomEmployee.setRegion(employee.getRegion());
        datacomEmployee.setFirstName(employee.getFirstName());
        datacomEmployee.setLastName(employee.getLastName());
        datacomEmployee.setTaxNumber(employee.getTaxNumber());
        datacomEmployee.setBankAccount(employee.getBankAccount());
        datacomEmployee.setStartDate(employee.getStartDate());
        datacomEmployee.setEndDate(employee.getEndDate());
        datacomEmployee.setPayGroup(employee.getPayGroup());
        datacomEmployee.setHourlyRate(employee.getHourlyRate());
        // Default values for new employees
        datacomEmployee.setEmploymentType(EmploymentType.PERMANENT_FULL_TIME);
        datacomEmployee.setTaxCode(TaxCode.M);
        datacomEmployee.setKiwiSaverStatus(KiwiSaverStatus.NOT_ELIGIBLE);
        datacomEmployee.setKiwiSaverRate(3.0);
        return datacomEmployee;
    }

    /**
     * Maps an HR absence to Datacom leave format.
     * Only processes leaves for NZ employees.
     */
    public DatacomLeave toDatacomLeave(Absence absence) {
        if (!isNZEmployee(absence.getEmployee())) {
            log.info("Skipping leave for non-NZ employee: {}", 
                absence.getEmployee().getEmployeeId());
            return null;
        }

        return DatacomLeave.builder()
            .employeeNumber(absence.getEmployee().getEmployeeId())
            .startDate(absence.getStartDate())
            .endDate(absence.getEndDate())
            .hours(absence.getHours())
            .notes(absence.getNotes())
            .leaveType(mapLeaveType(absence.getType()))
            .build();
    }

    private LeaveType mapLeaveType(AbsenceType absenceType) {
        return switch (absenceType) {
            case ANNUAL_LEAVE -> LeaveType.ANNUAL;
            case SICK_LEAVE -> LeaveType.SICK;
            case BEREAVEMENT_LEAVE -> LeaveType.BEREAVEMENT;
            case OTHER -> LeaveType.OTHER;
        };
    }

    private boolean isNZEmployee(Employee employee) {
        if (employee == null) {
            log.warn("Null employee provided");
            return false;
        }

        boolean isNZ = "NZ".equalsIgnoreCase(employee.getRegion());

        if (!isNZ) {
            log.debug("Employee {} is not a NZ employee. Region: {}", 
                employee.getEmployeeId(), 
                employee.getRegion());
        }

        return isNZ;
    }
} 