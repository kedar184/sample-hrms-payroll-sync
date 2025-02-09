package io.abc.hrms.payroll.service;

import io.abc.hrms.payroll.client.DatacomApiClient;
import io.abc.hrms.payroll.client.HRSystemClient;
import io.abc.hrms.payroll.mapper.DatacomMapper;
import io.abc.hrms.payroll.model.Employee;
import io.abc.hrms.payroll.model.Absence;
import io.abc.hrms.payroll.model.event.HREvent;
import io.abc.hrms.payroll.model.datacom.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.abc.hrms.payroll.util.LogContext;
import org.springframework.http.HttpStatus;
import io.abc.hrms.payroll.exception.EventProcessingException;
import io.abc.hrms.payroll.exception.DatacomApiException;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollSyncService {
    
    private final HRSystemClient hrSystemClient;
    private final DatacomApiClient datacomApiClient;
    private final DatacomMapper datacomMapper;
    private final MetricsService metricsService;
    private final NotificationService notificationService;
    
    public void processEmployeeEvent(HREvent event) {
        try {
            LogContext.setEventContext(event.getRecordId(), event.getRecordType(), event.getEventType());
            
            log.info("Processing employee event: {}", event.getEventType());
            log.debug("Event details: {}", event);
            
            Employee employee = hrSystemClient.getEmployee(event.getRecordId());
            DatacomEmployee datacomEmployee = datacomMapper.toDatacomEmployee(employee);
            
            if (datacomEmployee != null) {
                try {
                    log.debug("Mapped Datacom employee: {}", datacomEmployee);
                    
                    switch (event.getEventType()) {
                        case "EMPLOYEE_CREATED" -> handleNewEmployee(datacomEmployee);
                        case "EMPLOYEE_UPDATED" -> handleEmployeeUpdate(datacomEmployee);
                        case "EMPLOYEE_TERMINATED" -> handleEmployeeTermination(datacomEmployee);
                        default -> {
                            log.warn("Unhandled event type: {}", event.getEventType());
                            return;
                        }
                    }
                    
                    log.info("Successfully processed {} event", event.getEventType());
                    metricsService.recordEvent(event.getEventType(), true);
                } catch (DatacomApiException e) {
                    // Business error - notify but don't retry
                    notificationService.notifyBusinessError(event, e.getErrorCode(), e.getMessage());
                    // Don't rethrow - considered handled
                } catch (Exception e) {
                    // Technical error - rethrow for retry
                    throw new EventProcessingException("Technical error processing event", 
                        HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
                }
            } else {
                log.info("Skipping non-NZ employee");
            }
        } finally {
            LogContext.clear();
        }
    }
    
    private void handleNewEmployee(DatacomEmployee employee) {
        log.info("Creating new employee in Datacom");
        DatacomResponse response = datacomApiClient.updateEmployee(employee);
        log.info("New employee created in Datacom: {}", response);
    }
    
    private void handleEmployeeUpdate(DatacomEmployee employee) {
        log.info("Updating employee in Datacom");
        DatacomResponse response = datacomApiClient.updateEmployee(employee);
        log.info("Employee updated in Datacom: {}", response);
    }
    
    private void handleEmployeeTermination(DatacomEmployee employee) {
        log.info("Processing employee termination");
        // Ensure end date is set
        if (employee.getEndDate() == null) {
            employee.setEndDate(LocalDate.now());
        }
        DatacomResponse response = datacomApiClient.updateEmployee(employee);
        log.info("Employee terminated in Datacom: {}", response);
    }
    
    public void processAbsenceEvent(HREvent event) {
        try {
            LogContext.setEventContext(event.getRecordId(), event.getRecordType(), event.getEventType());
            
            log.info("Processing absence event: {}", event.getEventType());
            
            Absence absence = hrSystemClient.getAbsence(event.getRecordId());
            DatacomLeave leave = datacomMapper.toDatacomLeave(absence);
            
            if (leave != null) {
                try {
                    // Check if it's backdated
                    if (leave.getStartDate().isBefore(LocalDate.now())) {
                        log.info("Processing backdated leave for employee: {}", leave.getEmployeeNumber());
                    }
                    
                    datacomApiClient.submitLeave(leave);
                    log.info("Successfully processed leave event");
                    metricsService.recordEvent(event.getEventType(), true);
                } catch (Exception e) {
                    log.error("Failed to process leave event", e);
                    metricsService.recordEvent(event.getEventType(), false);
                    throw e;
                }
            }
        } finally {
            LogContext.clear();
        }
    }
} 