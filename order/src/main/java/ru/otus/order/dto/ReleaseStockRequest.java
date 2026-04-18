package ru.otus.order.dto;

public record ReleaseStockRequest(
        Long orderId,
        Long productId,
        Long quantity
) {}