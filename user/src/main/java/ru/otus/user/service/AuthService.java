package ru.otus.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.user.config.JwtUtil;
import ru.otus.user.dto.*;
import ru.otus.user.mapper.UserMapper;
import ru.otus.user.model.User;
import ru.otus.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        // Используем новый метод сервиса
        UserResponse userResponse = userService.createUserWithPassword(request);

        // Получаем пользователя для генерации токена
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found after creation"));

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, userResponse);
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, userMapper.toResponse(user));
    }
}