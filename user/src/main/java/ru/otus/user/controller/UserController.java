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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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

    private final Counter createUserCounter;
    private final Counter getUserCounter;
    private final Counter updateUserCounter;
    private final Counter deleteUserCounter;
    private final Counter getAllUsersCounter;
    private final Counter errorCounter;

    @Autowired
    public UserController(UserService userService, MeterRegistry meterRegistry) {
        this.userService = userService;
        this.meterRegistry = meterRegistry;

        this.createUserCounter = Counter.builder("user.api.calls")
                .tag("method", "createUser")
                .description("Total number of createUser calls")
                .register(meterRegistry);

        this.getUserCounter = Counter.builder("user.api.calls")
                .tag("method", "getUser")
                .description("Total number of getUser calls")
                .register(meterRegistry);

        this.updateUserCounter = Counter.builder("user.api.calls")
                .tag("method", "updateUser")
                .description("Total number of updateUser calls")
                .register(meterRegistry);

        this.deleteUserCounter = Counter.builder("user.api.calls")
                .tag("method", "deleteUser")
                .description("Total number of deleteUser calls")
                .register(meterRegistry);

        this.getAllUsersCounter = Counter.builder("user.api.calls")
                .tag("method", "getAllUsers")
                .description("Total number of getAllUsers calls")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("user.api.errors")
                .description("Total number of API errors")
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
    @Timed(value = "user.api.latency", extraTags = {"method", "createUser"})
    public UserResponse createUser(@Valid @RequestBody UserRequest userRequest) {
        createUserCounter.increment();
        try {
            return userService.createUser(userRequest);
        } catch (Exception e) {
            errorCounter.increment();
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
    @Timed(value = "user.api.latency", extraTags = {"method", "getUser"})
    public UserResponse getUser(
            @Parameter(description = "ID of the user to be retrieved", required = true, example = "1")
            @PathVariable Long id
    ) {
        getUserCounter.increment();
        try {
            return userService.getUserById(id);
        } catch (Exception e) {
            errorCounter.increment();
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
    @Timed(value = "user.api.latency", extraTags = {"method", "updateUser"})
    public UserResponse updateUser(
            @Parameter(description = "ID of the user to be updated", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest
    ) {
        updateUserCounter.increment();
        try {
            return userService.updateUser(id, userRequest);
        } catch (Exception e) {
            errorCounter.increment();
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
    @Timed(value = "user.api.latency", extraTags = {"method", "deleteUser"})
    public void deleteUser(
            @Parameter(description = "ID of the user to be deleted", required = true, example = "1")
            @PathVariable Long id
    ) {
        deleteUserCounter.increment();
        try {
            userService.deleteUser(id);
        } catch (Exception e) {
            errorCounter.increment();
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
    @Timed(value = "user.api.latency", extraTags = {"method", "getAllUsers"})
    public List<UserResponse> getAllUsers() {
        getAllUsersCounter.increment();
        try {
            return userService.getAllUsers();
        } catch (Exception e) {
            errorCounter.increment();
            throw e;
        }
    }
}