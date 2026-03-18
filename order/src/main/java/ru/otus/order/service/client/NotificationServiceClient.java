package ru.otus.order.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.otus.order.dto.NotificationRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.notification.url:http://notification-service:8002}")
    private String notificationServiceUrl;

    public void sendNotification(NotificationRequest request) {
        String url = notificationServiceUrl + "/api/v1/notifications";
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to send notification: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending notification", e);
        }
    }
}