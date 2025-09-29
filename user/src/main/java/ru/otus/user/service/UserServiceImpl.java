package ru.otus.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.otus.user.dto.RegisterRequest;
import ru.otus.user.exception.UserNotFoundException;
import ru.otus.user.mapper.UserMapper;
import ru.otus.user.model.User;
import ru.otus.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.user.dto.UserRequest;
import ru.otus.user.dto.UserResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        // Для обратной совместимости - генерируем случайный пароль
        String randomPassword = java.util.UUID.randomUUID().toString().substring(0, 12);
        User user = new User(userRequest.name(), userRequest.email(), passwordEncoder.encode(randomPassword));
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse createUserWithPassword(RegisterRequest registerRequest) {
        User user = new User(
                registerRequest.name(),
                registerRequest.email(),
                passwordEncoder.encode(registerRequest.password())
        );
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setName(userRequest.name());
        user.setEmail(userRequest.email());

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }
}
