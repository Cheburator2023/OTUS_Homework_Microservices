package ru.otus.notification.dto;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long userId,
        String email,
        String message,
        Instant createdAt
) {}