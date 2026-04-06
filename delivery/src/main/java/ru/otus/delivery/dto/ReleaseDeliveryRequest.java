package ru.otus.delivery.dto;

import jakarta.validation.constraints.NotNull;

public record ReleaseDeliveryRequest(
        @NotNull Long orderId
) {}