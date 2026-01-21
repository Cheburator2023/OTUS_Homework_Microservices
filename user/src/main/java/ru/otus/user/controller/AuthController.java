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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.otus.user.dto.*;
import ru.otus.user.mapper.UserMapper;
import ru.otus.user.model.User;
import ru.otus.user.repository.UserRepository;
import ru.otus.user.security.JwtTokenProvider;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Authentication and registration operations")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final MeterRegistry meterRegistry;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Timed(value = "user_api_latency_seconds", extraTags = {"method", "register"})
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        Counter counter = buildApiCounter("register", "201");
        counter.increment();

        try {
            if (userRepository.existsByEmail(request.email())) {
                buildErrorCounter("register", "400").increment();
                throw new RuntimeException("Email already exists");
            }

            User user = new User(request.name(), request.email(), passwordEncoder.encode(request.password()));
            User savedUser = userRepository.save(user);

            String token = jwtTokenProvider.generateToken(savedUser);
            UserResponse userResponse = userMapper.toResponse(savedUser);

            return new AuthResponse(token, userResponse);
        } catch (Exception e) {
            buildErrorCounter("register", "500").increment();
            throw e;
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Timed(value = "user_api_latency_seconds", extraTags = {"method", "login"})
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        Counter counter = buildApiCounter("login", "200");
        counter.increment();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();

            String token = jwtTokenProvider.generateToken(user);
            UserResponse userResponse = userMapper.toResponse(user);

            return new AuthResponse(token, userResponse);
        } catch (Exception e) {
            buildErrorCounter("login", "401").increment();
            throw new RuntimeException("Invalid email or password");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates user session (client should discard token)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Timed(value = "user_api_latency_seconds", extraTags = {"method", "logout"})
    public void logout() {
        Counter counter = buildApiCounter("logout", "200");
        counter.increment();

        SecurityContextHolder.clearContext();
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