package ru.otus.billing.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull Long userId,
        String email
) {}