package ru.otus.user.controller;

import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.otus.user.dto.AuthResponse;
import ru.otus.user.dto.ProfileUpdateRequest;
import ru.otus.user.dto.UserResponse;
import ru.otus.user.model.User;
import ru.otus.user.service.ProfileService;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile API", description = "User profile operations")
public class ProfileController {
    private final ProfileService profileService;

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
        return profileService.getProfile(currentUser.getId());
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
    public AuthResponse updateProfile(@AuthenticationPrincipal User currentUser,
                                      @Valid @RequestBody ProfileUpdateRequest request) {
        return profileService.updateProfile(currentUser.getId(), request);
    }
}