package ru.otus.order.dto;

import java.math.BigDecimal;

public record WithdrawRequest(BigDecimal amount) {}