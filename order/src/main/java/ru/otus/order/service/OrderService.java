package ru.otus.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.order.dto.*;
import ru.otus.order.model.IdempotencyRecord;
import ru.otus.order.model.Order;
import ru.otus.order.model.Order.OrderStatus;
import ru.otus.order.repository.IdempotencyRepository;
import ru.otus.order.repository.OrderRepository;
import ru.otus.order.service.client.BillingServiceClient;
import ru.otus.order.service.client.DeliveryServiceClient;
import ru.otus.order.service.client.NotificationServiceClient;
import ru.otus.order.service.client.StockServiceClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final BillingServiceClient billingClient;
    private final NotificationServiceClient notificationClient;
    private final StockServiceClient stockClient;
    private final DeliveryServiceClient deliveryClient;

    @Transactional
    public OrderResponse createOrder(String idempotencyKey, CreateOrderRequest request) {
        log.info("=== CREATE ORDER REQUEST (idempotent) ===");
        log.info("IdempotencyKey: {}, userId: {}, amount: {}, productId: {}, quantity: {}, deliveryTime: {}",
                idempotencyKey, request.userId(), request.amount(), request.productId(),
                request.quantity(), request.deliveryTime());

        // 1. Проверяем, не обрабатывался ли уже этот ключ
        var existingRecord = idempotencyRepository.findByIdempotencyKey(idempotencyKey);
        if (existingRecord.isPresent()) {
            Order existingOrder = existingRecord.get().getOrder();
            log.info("Idempotency key {} already processed, returning existing order {}", idempotencyKey, existingOrder.getId());
            return toResponse(existingOrder);
        }

        // 2. Создаём заказ со статусом FAILED (временный)
        Order order = new Order(
                request.userId(),
                request.amount(),
                OrderStatus.FAILED,
                request.productId(),
                request.quantity(),
                request.deliveryTime()
        );
        order = orderRepository.save(order);
        Long orderId = order.getId();
        log.info("Order created with id: {} (temporary FAILED)", orderId);

        boolean withdrawSuccess = false;
        boolean stockReserved = false;
        boolean deliveryReserved = false;
        String notificationMessage = null;

        try {
            // Шаг 2: списание средств
            log.info("Step 1: Withdraw from billing for user {}", request.userId());
            withdrawSuccess = billingClient.withdraw(request.userId(), request.amount());
            if (!withdrawSuccess) {
                log.warn("Withdraw failed for user {}", request.userId());
                notificationMessage = "Failed to process your order due to insufficient funds or billing error.";
                sendNotification(request, notificationMessage);
                // Сохраняем идемпотентную запись даже для неудачного заказа
                saveIdempotencyRecord(idempotencyKey, order);
                return toResponse(order);
            }
            log.info("Withdraw successful");

            // Шаг 3: резервирование товара на складе
            log.info("Step 2: Reserve stock for product {}", request.productId());
            ReserveStockRequest stockRequest = new ReserveStockRequest(
                    orderId, request.productId(), request.quantity());
            ResponseEntity<Void> stockResponse = stockClient.reserve(stockRequest);
            if (!stockResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Stock reservation failed with status: " + stockResponse.getStatusCode());
            }
            stockReserved = true;
            log.info("Stock reserved successfully");

            // Шаг 4: резервирование слота доставки
            log.info("Step 3: Reserve delivery slot for time {}", request.deliveryTime());
            ReserveDeliveryRequest deliveryRequest = new ReserveDeliveryRequest(
                    orderId, request.deliveryTime()
            );
            ResponseEntity<Void> deliveryResponse = deliveryClient.reserve(deliveryRequest);
            if (!deliveryResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Delivery reservation failed with status: " + deliveryResponse.getStatusCode());
            }
            deliveryReserved = true;
            log.info("Delivery slot reserved successfully");

            // Все шаги выполнены успешно → меняем статус заказа на SUCCESS
            order.setStatus(OrderStatus.SUCCESS);
            order = orderRepository.save(order);
            notificationMessage = "Your order has been successfully processed.";
            log.info("Order {} completed successfully", orderId);

        } catch (Exception e) {
            log.error("Error during order creation for orderId: {}", orderId, e);
            notificationMessage = "Failed to process your order: " + e.getMessage();

            // Компенсация: откатываем уже выполненные шаги в обратном порядке
            if (deliveryReserved) {
                try {
                    ReleaseDeliveryRequest releaseDelivery = new ReleaseDeliveryRequest(orderId);
                    deliveryClient.release(releaseDelivery);
                    log.debug("Delivery release compensation done for order {}", orderId);
                } catch (Exception ex) {
                    log.error("Failed to release delivery during compensation for order {}", orderId, ex);
                }
            }
            if (stockReserved) {
                try {
                    ReleaseStockRequest releaseStock = new ReleaseStockRequest(
                            orderId, request.productId(), request.quantity()
                    );
                    stockClient.release(releaseStock);
                    log.debug("Stock release compensation done for order {}", orderId);
                } catch (Exception ex) {
                    log.error("Failed to release stock during compensation for order {}", orderId, ex);
                }
            }
            if (withdrawSuccess) {
                try {
                    billingClient.deposit(request.userId(), request.amount());
                    log.debug("Deposit compensation done for user {}", request.userId());
                } catch (Exception ex) {
                    log.error("Failed to deposit during compensation for user {}", request.userId(), ex);
                }
            }
            // Статус заказа остаётся FAILED (уже сохранён)
        }

        // Сохраняем идемпотентную запись (всегда после окончательной фиксации заказа)
        saveIdempotencyRecord(idempotencyKey, order);

        if (notificationMessage != null) {
            sendNotification(request, notificationMessage);
        }

        log.info("=== END createOrder, order status = {}", order.getStatus());
        return toResponse(order);
    }

    private void saveIdempotencyRecord(String idempotencyKey, Order order) {
        try {
            IdempotencyRecord record = new IdempotencyRecord(idempotencyKey, order);
            idempotencyRepository.save(record);
        } catch (Exception e) {
            log.error("Failed to save idempotency record for key {}", idempotencyKey, e);
        }
    }

    private void sendNotification(CreateOrderRequest request, String message) {
        NotificationRequest notification = new NotificationRequest(
                request.userId(),
                request.email(),
                message
        );
        try {
            notificationClient.sendNotification(notification);
            log.debug("Notification sent to user {}", request.userId());
        } catch (Exception e) {
            log.error("Failed to send notification to user {}", request.userId(), e);
        }
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
