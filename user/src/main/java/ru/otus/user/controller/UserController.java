package ru.otus.user.controller;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.otus.user.dto.UserRequest;
import ru.otus.user.dto.UserResponse;
import ru.otus.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "Operations with users")
public class UserController {
    private final UserService userService;
    private final MeterRegistry meterRegistry;

    private Counter buildApiCounter(String method,String statusCode) {
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user", description = "Creates and saves a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @Timed(value = "user_api_latency_seconds", extraTags = {"method", "createUser"})
    public UserResponse createUser(@Valid @RequestBody UserRequest userRequest) {
        Counter counter = buildApiCounter("createUser", "201");
        counter.increment();

        try {
            return userService.createUser(userRequest);
        } catch (Exception e) {
            String statusCode = e instanceof jakarta.validation.ValidationException ? "400" : "500";
            buildErrorCounter("createUser", statusCode).increment();
            throw e;
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Returns a single user by their identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @Timed(value = "user_api_latency_seconds", extraTags = {"method", "getUser"})
    public UserResponse getUser(
            @Parameter(description = "ID of the user to be retrieved", required = true, example = "1")
            @PathVariable Long id
    ) {
        Counter counter = buildApiCounter("getUser", "200");
        counter.increment();

        try {
            return userService.getUserById(id);
        } catch (Exception e) {
            String statusCode = e instanceof jakarta.persistence.EntityNotFoundException ? "404" : "500";
            buildErrorCounter("getUser", statusCode).increment();
            throw e;
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @Timed(value = "user_api_latency_seconds", extraTags = {"method", "updateUser"})
    public UserResponse updateUser(
            @Parameter(description = "ID of the user to be updated", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest
    ) {
        Counter counter = buildApiCounter("updateUser", "200");
        counter.increment();

        try {
            return userService.updateUser(id, userRequest);
        } catch (Exception e) {
            String statusCode = e instanceof jakarta.persistence.EntityNotFoundException ? "404" :
                    e instanceof jakarta.validation.ValidationException ? "400" : "500";
            buildErrorCounter("updateUser", statusCode).increment();
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user", description = "Deletes a user by their identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @Timed(value = "user_api_latency_seconds", extraTags = {"method", "deleteUser"})
    public void deleteUser(
            @Parameter(description = "ID of the user to be deleted", required = true, example = "1")
            @PathVariable Long id
    ) {
        Counter counter = buildApiCounter("deleteUser", "204");
        counter.increment();

        try {
            userService.deleteUser(id);
        } catch (Exception e) {
            String statusCode = e instanceof jakarta.persistence.EntityNotFoundException ? "404" : "500";
            buildErrorCounter("deleteUser", statusCode).increment();
            throw e;
        }
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Returns list of all available users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(schema = @Schema(implementation = UserResponse[].class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @Timed(value = "user_api_latency_seconds", extraTags = {"method", "getAllUsers"})
    public List<UserResponse> getAllUsers() {
        Counter counter = buildApiCounter("getAllUsers", "200");
        counter.increment();

        try {
            return userService.getAllUsers();
        } catch (Exception e) {
            buildErrorCounter("getAllUsers", "500").increment();
            throw e;
        }
    }
}