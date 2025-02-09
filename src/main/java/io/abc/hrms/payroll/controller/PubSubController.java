package io.abc.hrms.payroll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.abc.hrms.payroll.model.event.HREvent;
import io.abc.hrms.payroll.service.PayrollSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.abc.hrms.payroll.exception.EventProcessingException;

import java.util.Base64;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/hrms")
public class PubSubController {

    private final PayrollSyncService payrollSyncService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/events", consumes = "application/json")
    public ResponseEntity<Void> handlePushMessage(@RequestBody PubSubMessage message) {
        try {
            String decodedData = new String(Base64.getDecoder().decode(message.data));
            HREvent event = objectMapper.readValue(decodedData, HREvent.class);
            
            if ("EMPLOYEE".equals(event.getRecordType())) {
                payrollSyncService.processEmployeeEvent(event);
            } else if ("ABSENCE".equals(event.getRecordType())) {
                payrollSyncService.processAbsenceEvent(event);
            } else {
                // Return 200 for unknown event types (PubSub will not retry)
                log.warn("Unknown event type: {}", event.getRecordType());
                return ResponseEntity.ok().build();
            }
            
            return ResponseEntity.ok().build();
            
        } catch (IllegalArgumentException e) {
            // Bad message format - don't retry
            log.error("Invalid message format", e);
            return ResponseEntity.badRequest().build();
            
        } catch (EventProcessingException e) {
            // Return appropriate status based on the error
            log.error("Event processing failed", e);
            return ResponseEntity.status(e.getStatusCode()).build();
            
        } catch (Exception e) {
            // Unexpected error - retry
            log.error("Unexpected error processing message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Pub/Sub message format
    record PubSubMessage(String messageId, String publishTime, String data) {}
} 