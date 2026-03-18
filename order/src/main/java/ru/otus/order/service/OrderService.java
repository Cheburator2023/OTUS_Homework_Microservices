package ru.otus.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.order.dto.CreateOrderRequest;
import ru.otus.order.dto.NotificationRequest;
import ru.otus.order.dto.OrderResponse;
import ru.otus.order.model.Order;
import ru.otus.order.model.Order.OrderStatus;
import ru.otus.order.repository.OrderRepository;
import ru.otus.order.service.client.BillingServiceClient;
import ru.otus.order.service.client.NotificationServiceClient;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final BillingServiceClient billingClient;
    private final NotificationServiceClient notificationClient;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Сначала пытаемся списать деньги
        boolean withdrawSuccess = billingClient.withdraw(request.userId(), request.amount());

        // 2. Определяем статус заказа и сообщение
        OrderStatus status;
        String message;
        if (withdrawSuccess) {
            status = OrderStatus.SUCCESS;
            message = "Your order of $" + request.amount() + " has been successfully processed.";
        } else {
            status = OrderStatus.FAILED;
            message = "Failed to process your order of $" + request.amount() + " due to insufficient funds.";
        }

        // 3. Сохраняем заказ
        Order order = new Order(request.userId(), request.amount(), status);
        order = orderRepository.save(order);

        // 4. Отправляем уведомление
        String email = request.email();

        NotificationRequest notification = new NotificationRequest(
                request.userId(),
                email,
                message
        );
        notificationClient.sendNotification(notification);

        return toResponse(order);
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