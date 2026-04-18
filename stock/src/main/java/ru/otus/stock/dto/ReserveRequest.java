package ru.otus.stock.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReserveRequest(
        @NotNull Long productId,
        @NotNull @Positive Integer quantity,
        @NotNull Long orderId
) {}