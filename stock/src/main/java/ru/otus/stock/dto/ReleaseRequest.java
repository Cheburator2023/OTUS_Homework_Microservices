package ru.otus.stock.dto;

import jakarta.validation.constraints.NotNull;

public record ReleaseRequest(
        @NotNull Long orderId,
        @NotNull Long productId,
        @NotNull Integer quantity
) {}