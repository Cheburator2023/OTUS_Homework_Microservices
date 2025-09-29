package ru.otus.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.otus.user.dto.*;
import ru.otus.user.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "User registration and authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.authenticate(request);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public void logout(@RequestHeader("Authorization") String token) {
        // В stateless JWT логаут на клиенте - просто удаляем токен
    }
}