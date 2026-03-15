package ru.otus.order.dto;

public record NotificationRequest(
        Long userId,
        String email,
        String message
) {}