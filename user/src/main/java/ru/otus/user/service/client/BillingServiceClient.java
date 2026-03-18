package ru.otus.user.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.billing.url:http://billing-service:8001}")
    private String billingServiceUrl;

    public void createAccount(Long userId, String email) {
        String url = billingServiceUrl + "/api/v1/accounts";
        CreateAccountRequest request = new CreateAccountRequest(userId, email);
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to create account in billing service for userId: {}, status: {}", userId, response.getStatusCode());
                // По условию считаем, что сервисы не падают, поэтому просто логируем
            }
        } catch (Exception e) {
            log.error("Error calling billing service for userId: {}", userId, e);
        }
    }

    // Внутренний DTO
    public record CreateAccountRequest(Long userId, String email) {}
}