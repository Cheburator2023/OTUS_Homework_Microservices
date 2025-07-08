package ru.otus.user.dto;

public record ErrorResponse(
        String code,
        String message
) {}
