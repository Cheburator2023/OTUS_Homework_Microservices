package ru.otus.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.user.dto.AuthResponse;
import ru.otus.user.dto.ProfileUpdateRequest;
import ru.otus.user.dto.UserResponse;
import ru.otus.user.exception.EmailAlreadyExistsException;
import ru.otus.user.exception.UserNotFoundException;
import ru.otus.user.mapper.UserMapper;
import ru.otus.user.model.User;
import ru.otus.user.repository.UserRepository;
import ru.otus.user.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MetricService metricService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        metricService.buildApiCounter("getProfile", "200").increment();

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            return userMapper.toResponse(user);
        } catch (Exception e) {
            metricService.buildErrorCounter("getProfile", "404").increment();
            throw e;
        }
    }

    @Transactional
    public AuthResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        metricService.buildApiCounter("updateProfile", "200").increment();

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Проверяем, не занят ли email другим пользователем
            if (!user.getEmail().equals(request.email()) &&
                    userRepository.existsByEmail(request.email())) {
                metricService.buildErrorCounter("updateProfile", "400").increment();
                throw new RuntimeException("Email already exists");
            }

            user.setName(request.name());
            user.setEmail(request.email());

            User updatedUser = userRepository.save(user);

            // Генерируем новый токен с обновлёнными данными
            String newToken = jwtTokenProvider.generateToken(updatedUser);
            UserResponse userResponse = userMapper.toResponse(updatedUser);

            return new AuthResponse(newToken, userResponse);
        } catch (Exception e) {
            String statusCode = e instanceof UserNotFoundException ? "404" : "500";
            metricService.buildErrorCounter("updateProfile", statusCode).increment();
            throw e;
        }
    }
}