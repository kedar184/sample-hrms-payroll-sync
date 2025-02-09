package io.abc.hrms.payroll.service;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import io.abc.hrms.payroll.model.event.HREvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final ObjectMapper objectMapper;
    private final PubSubTemplate pubSubTemplate;
    
    @Value("${notification.pubsub.enabled:false}")
    private boolean pubsubEnabled;
    
    @Value("${notification.topic:business-notifications}")
    private String notificationTopic;
    
    public void notifyBusinessError(HREvent event, String errorCode, String message) {
        Map<String, Object> notification = Map.of(
            "recordType", event.getRecordType(),
            "recordId", event.getRecordId(),
            "errorCode", errorCode,
            "message", message
        );

        try {
            String jsonNotification = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(notification);
            log.info("Business error:\n{}", jsonNotification);
            
            if (pubsubEnabled) {
                CompletableFuture<String> future = pubSubTemplate.publish(notificationTopic, notification);
                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish notification", ex);
                    } else {
                        log.debug("Published notification for {}", event.getRecordId());
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error processing notification", e);
        }
    }
} 