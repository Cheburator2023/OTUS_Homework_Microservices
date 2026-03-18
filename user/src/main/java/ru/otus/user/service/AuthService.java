package ru.otus.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.user.dto.*;
import ru.otus.user.exception.EmailAlreadyExistsException;
import ru.otus.user.exception.InvalidCredentialsException;
import ru.otus.user.mapper.UserMapper;
import ru.otus.user.model.User;
import ru.otus.user.repository.UserRepository;
import ru.otus.user.security.JwtTokenProvider;
import ru.otus.user.service.client.BillingServiceClient;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final MetricService metricService;
    private final BillingServiceClient billingServiceClient;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        metricService.buildApiCounter("register", "201").increment();

        try {
            if (userRepository.existsByEmail(request.email())) {
                metricService.buildErrorCounter("register", "400").increment();
                throw new EmailAlreadyExistsException("Email already exists");
            }

            User user = new User(request.name(), request.email(),
                    passwordEncoder.encode(request.password()));
            User savedUser = userRepository.save(user);

            billingServiceClient.createAccount(savedUser.getId(), savedUser.getEmail());

            String token = jwtTokenProvider.generateToken(savedUser);
            UserResponse userResponse = userMapper.toResponse(savedUser);

            return new AuthResponse(token, userResponse);
        } catch (Exception e) {
            metricService.buildErrorCounter("register", "500").increment();
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        metricService.buildApiCounter("login", "200").increment();

        try {
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                metricService.buildErrorCounter("login", "401").increment();
                throw new InvalidCredentialsException("Invalid email or password");
            }

            String token = jwtTokenProvider.generateToken(user);
            UserResponse userResponse = userMapper.toResponse(user);

            return new AuthResponse(token, userResponse);
        } catch (Exception e) {
            metricService.buildErrorCounter("login", "401").increment();
            throw e;
        }
    }
}