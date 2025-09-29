package ru.otus.user.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {}
