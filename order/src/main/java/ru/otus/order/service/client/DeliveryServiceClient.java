package ru.otus.order.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.otus.order.dto.ReleaseDeliveryRequest;
import ru.otus.order.dto.ReserveDeliveryRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.delivery.url:http://delivery-service:8005}")
    private String deliveryServiceUrl;

    public ResponseEntity<Void> reserve(ReserveDeliveryRequest request) {
        String url = deliveryServiceUrl + "/api/v1/delivery/reserve";
        log.info("Calling delivery service: POST {}", url);
        log.debug("Request body: orderId={}, deliveryTime={}", request.orderId(), request.deliveryTime());
        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
        log.info("Delivery service responded with status: {}", response.getStatusCode());
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Delivery reservation failed with status {}: {}", response.getStatusCode(), response.getBody());
        }
        return response;
    }

    public void release(ReleaseDeliveryRequest request) {
        String url = deliveryServiceUrl + "/api/v1/delivery/release";
        try {
            restTemplate.postForEntity(url, request, Void.class);
        } catch (Exception e) {
            log.error("Delivery release failed for order {}: {}", request.orderId(), e.getMessage());
        }
    }
}