package ru.otus.billing.dto;

public record ErrorResponse(
        String code,
        String message
) {}
