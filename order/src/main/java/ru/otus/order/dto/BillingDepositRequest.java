package ru.otus.order.dto;

import java.math.BigDecimal;

public record BillingDepositRequest(
        BigDecimal amount
) {}