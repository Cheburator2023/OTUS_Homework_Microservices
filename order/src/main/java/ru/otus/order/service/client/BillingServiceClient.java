package ru.otus.order.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.otus.order.dto.BillingDepositRequest;
import ru.otus.order.dto.WithdrawRequest;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.billing.url:http://billing-service:8001}")
    private String billingServiceUrl;

    public boolean withdraw(Long userId, java.math.BigDecimal amount) {
        String url = billingServiceUrl + "/api/v1/accounts/" + userId + "/withdraw";
        WithdrawRequest request = new WithdrawRequest(amount);
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Withdraw failed for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    public void deposit(Long userId, BigDecimal amount) {
        String url = billingServiceUrl + "/api/v1/accounts/" + userId + "/deposit";
        BillingDepositRequest request = new BillingDepositRequest(amount);
        try {
            restTemplate.postForEntity(url, request, Void.class);
        } catch (Exception e) {
            log.error("Deposit failed for user {}: {}", userId, e.getMessage());
            throw e;
        }
    }
}