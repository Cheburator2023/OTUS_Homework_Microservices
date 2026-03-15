package ru.otus.billing.dto;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        Long userId,
        BigDecimal balance
) {}