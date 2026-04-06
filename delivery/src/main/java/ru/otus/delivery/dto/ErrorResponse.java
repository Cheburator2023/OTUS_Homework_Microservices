package ru.otus.delivery.dto;

public record ErrorResponse(
        String code,
        String message
) {}
