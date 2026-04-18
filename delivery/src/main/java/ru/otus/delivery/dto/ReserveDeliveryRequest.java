package ru.otus.delivery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ReserveDeliveryRequest(
        @NotNull Long orderId,
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime deliveryTime
) {}