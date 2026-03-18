package ru.otus.notification.dto;

public record ErrorResponse(
        String code,
        String message
) {}
