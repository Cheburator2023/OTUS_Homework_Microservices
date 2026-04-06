package ru.otus.order.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.otus.order.dto.ReleaseStockRequest;
import ru.otus.order.dto.ReserveStockRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.stock.url:http://stock-service:8004}")
    private String stockServiceUrl;

    public ResponseEntity<Void> reserve(ReserveStockRequest request) {
        String url = stockServiceUrl + "/api/v1/stock/reserve";
        log.info("Calling stock service: POST {}", url);
        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
        log.info("Stock service responded with status: {}", response.getStatusCode());
        return response;
    }

    public void release(ReleaseStockRequest request) {
        String url = stockServiceUrl + "/api/v1/stock/release";
        try {
            restTemplate.postForEntity(url, request, Void.class);
        } catch (Exception e) {
            log.error("Stock release failed for order {}: {}", request.orderId(), e.getMessage());
        }
    }
}