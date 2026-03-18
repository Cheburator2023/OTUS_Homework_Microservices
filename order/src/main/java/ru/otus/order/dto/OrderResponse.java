package ru.otus.order.dto;

import ru.otus.order.model.Order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        Long userId,
        BigDecimal amount,
        OrderStatus status,
        Instant createdAt
) {}