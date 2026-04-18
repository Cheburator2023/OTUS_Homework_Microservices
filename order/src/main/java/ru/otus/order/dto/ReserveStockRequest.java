package ru.otus.order.dto;

public record ReserveStockRequest(
        Long orderId,
        Long productId,
        Long quantity
) {}