package io.abc.hrms.payroll.service;

import io.abc.hrms.payroll.client.DatacomApiClient;
import io.abc.hrms.payroll.client.HRSystemClient;
import io.abc.hrms.payroll.mapper.DatacomMapper;
import io.abc.hrms.payroll.model.*;
import io.abc.hrms.payroll.model.datacom.*;
import io.abc.hrms.payroll.model.event.HREvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollSyncServiceTest {

    @Mock private HRSystemClient hrSystemClient;
    @Mock private DatacomApiClient datacomApiClient;
    @Mock private DatacomMapper datacomMapper;
    @Mock private MetricsService metricsService;
    @Mock private NotificationService notificationService;
    
    private PayrollSyncService payrollSyncService;
    
    @BeforeEach
    void setUp() {
        payrollSyncService = new PayrollSyncService(hrSystemClient, datacomApiClient, datacomMapper, metricsService, notificationService);
    }
    
    @Test
    void whenNewEmployeeOnboarded_shouldSyncToDatacom() {
        // Arrange
        HREvent event = createEvent("EMPLOYEE_CREATED", "EMPLOYEE", "EMP001");
        Employee employee = createEmployee("EMP001");
        DatacomEmployee datacomEmployee = createDatacomEmployee("EMP001");
        DatacomResponse response = new DatacomResponse(true, "EMP001", "Success");
        
        when(hrSystemClient.getEmployee("EMP001")).thenReturn(employee);
        when(datacomMapper.toDatacomEmployee(employee)).thenReturn(datacomEmployee);
        when(datacomApiClient.updateEmployee(datacomEmployee)).thenReturn(response);
        
        // Act
        payrollSyncService.processEmployeeEvent(event);
        
        // Assert
        verify(datacomApiClient).updateEmployee(datacomEmployee);
        verify(metricsService).recordEvent("EMPLOYEE_CREATED", true);
    }
    
    @Test
    void whenEmployeeTerminated_shouldUpdateDatacomWithEndDate() {
        // Arrange
        HREvent event = createEvent("EMPLOYEE_TERMINATED", "EMPLOYEE", "EMP001");
        Employee employee = createTerminatedEmployee("EMP001");
        DatacomEmployee datacomEmployee = createDatacomEmployee("EMP001");
        datacomEmployee.setEndDate(LocalDate.now());
        
        when(hrSystemClient.getEmployee("EMP001")).thenReturn(employee);
        when(datacomMapper.toDatacomEmployee(employee)).thenReturn(datacomEmployee);
        
        // Act
        payrollSyncService.processEmployeeEvent(event);
        
        // Assert
        verify(datacomApiClient).updateEmployee(datacomEmployee);
        verify(metricsService).recordEvent("EMPLOYEE_TERMINATED", true);
    }
    
    @Test
    void whenNonNZEmployee_shouldSkipProcessing() {
        // Arrange
        HREvent event = createEvent("EMPLOYEE_CREATED", "EMPLOYEE", "EMP001");
        Employee employee = createEmployee("EMP001");
        employee.setRegion("AU");
        
        when(hrSystemClient.getEmployee("EMP001")).thenReturn(employee);
        when(datacomMapper.toDatacomEmployee(employee)).thenReturn(null);
        
        // Act
        payrollSyncService.processEmployeeEvent(event);
        
        // Assert
        verify(datacomApiClient, never()).updateEmployee(any());
        verify(metricsService, never()).recordEvent(any(), anyBoolean());
    }
    
    @Test
    void whenLeaveRequested_shouldSyncToDatacom() {
        // Arrange
        HREvent event = createEvent("ABSENCE_CREATED", "ABSENCE", "ABS001");
        Absence absence = createAbsence("ABS001", "EMP001");
        DatacomLeave leave = createDatacomLeave("EMP001");
        
        when(hrSystemClient.getAbsence("ABS001")).thenReturn(absence);
        when(datacomMapper.toDatacomLeave(absence)).thenReturn(leave);
        
        // Act
        payrollSyncService.processAbsenceEvent(event);
        
        // Assert
        verify(datacomApiClient).submitLeave(leave);
        verify(metricsService).recordEvent("ABSENCE_CREATED", true);
    }

    // Helper methods
    private HREvent createEvent(String eventType, String recordType, String recordId) {
        HREvent event = new HREvent();
        event.setEventType(eventType);
        event.setRecordType(recordType);
        event.setRecordId(recordId);
        event.setTimestamp(LocalDate.now().toString());
        return event;
    }
    
    private Employee createEmployee(String id) {
        Employee employee = new Employee();
        employee.setEmployeeId(id);
        employee.setFirstName("Test");
        employee.setLastName("User");
        employee.setRegion("NZ");
        return employee;
    }
    
    private Employee createTerminatedEmployee(String id) {
        Employee employee = createEmployee(id);
        employee.setEndDate(LocalDate.now());
        return employee;
    }
    
    private DatacomEmployee createDatacomEmployee(String id) {
        return DatacomEmployee.builder()
            .employeeNumber(id)
            .firstName("Test")
            .lastName("User")
            .build();
    }
    
    private Absence createAbsence(String absenceId, String employeeId) {
        Absence absence = new Absence();
        absence.setEmployee(createEmployee(employeeId));
        absence.setStartDate(LocalDate.now().plusDays(1));
        absence.setEndDate(LocalDate.now().plusDays(2));
        absence.setType(AbsenceType.ANNUAL_LEAVE);
        return absence;
    }
    
    private DatacomLeave createDatacomLeave(String employeeId) {
        return DatacomLeave.builder()
            .employeeNumber(employeeId)
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusDays(2))
            .leaveType(LeaveType.ANNUAL)
            .build();
    }
} 