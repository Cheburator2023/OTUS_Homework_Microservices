package ru.otus.user.controller;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.otus.user.dto.ProfileUpdateRequest;
import ru.otus.user.dto.UserResponse;
import ru.otus.user.mapper.UserMapper;
import ru.otus.user.model.User;
import ru.otus.user.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile API", description = "User profile operations")
public class ProfileController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MeterRegistry meterRegistry;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile", description = "Returns the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Timed(value = "user_api_latency_seconds", extraTags = {"method", "getProfile"})
    public UserResponse getProfile(@AuthenticationPrincipal User currentUser) {
        Counter counter = buildApiCounter("getProfile", "200");
        counter.increment();

        try {
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return userMapper.toResponse(user);
        } catch (Exception e) {
            buildErrorCounter("getProfile", "404").increment();
            throw e;
        }
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile", description = "Updates the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Timed(value = "user_api_latency_seconds", extraTags = {"method", "updateProfile"})
    public UserResponse updateProfile(@AuthenticationPrincipal User currentUser,
                                      @Valid @RequestBody ProfileUpdateRequest request) {
        Counter counter = buildApiCounter("updateProfile", "200");
        counter.increment();

        try {
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setName(request.name());
            user.setEmail(request.email());

            User updatedUser = userRepository.save(user);
            return userMapper.toResponse(updatedUser);
        } catch (Exception e) {
            String statusCode = e.getMessage().contains("not found") ? "404" : "500";
            buildErrorCounter("updateProfile", statusCode).increment();
            throw e;
        }
    }

    private Counter buildApiCounter(String method, String statusCode) {
        return Counter.builder("user_api_calls")
                .tag("method", method)
                .tag("status_code", statusCode)
                .description("Total number of " + method + " calls")
                .register(meterRegistry);
    }

    private Counter buildErrorCounter(String method, String statusCode) {
        return Counter.builder("user_api_errors")
                .tag("method", method)
                .tag("status_code", statusCode)
                .description("Number of API errors for " + method)
                .register(meterRegistry);
    }
}