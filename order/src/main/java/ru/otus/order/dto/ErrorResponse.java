package ru.otus.order.dto;

public record ErrorResponse(
        String code,
        String message
) {}
