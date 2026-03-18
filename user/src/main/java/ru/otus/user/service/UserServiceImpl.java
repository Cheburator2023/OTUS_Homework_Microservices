package ru.otus.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.user.dto.UserRequest;
import ru.otus.user.dto.UserResponse;
import ru.otus.user.exception.EmailAlreadyExistsException;
import ru.otus.user.exception.UserNotFoundException;
import ru.otus.user.mapper.UserMapper;
import ru.otus.user.model.User;
import ru.otus.user.repository.UserRepository;
import ru.otus.user.service.client.BillingServiceClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MetricService metricService;
    private final BillingServiceClient billingServiceClient;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        metricService.buildApiCounter("createUser", "201").increment();

        try {
            if (userRepository.existsByEmail(userRequest.email())) {
                metricService.buildErrorCounter("createUser", "400").increment();
                throw new EmailAlreadyExistsException("Email already exists");
            }

            String randomPassword = generateRandomPassword();
            User user = new User(userRequest.name(), userRequest.email(),
                    passwordEncoder.encode(randomPassword));
            User savedUser = userRepository.save(user);

            billingServiceClient.createAccount(savedUser.getId(), savedUser.getEmail());

            return userMapper.toResponse(savedUser);
        } catch (Exception e) {
            metricService.buildErrorCounter("createUser", "500").increment();
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        metricService.buildApiCounter("getUser", "200").increment();

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
            return userMapper.toResponse(user);
        } catch (Exception e) {
            metricService.buildErrorCounter("getUser", "404").increment();
            throw e;
        }
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        metricService.buildApiCounter("updateUser", "200").increment();

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

            // Проверяем, не занят ли email другим пользователем
            if (!user.getEmail().equals(userRequest.email()) &&
                    userRepository.existsByEmail(userRequest.email())) {
                metricService.buildErrorCounter("updateUser", "400").increment();
                throw new EmailAlreadyExistsException("Email already exists");
            }

            user.setName(userRequest.name());
            user.setEmail(userRequest.email());

            User updatedUser = userRepository.save(user);
            return userMapper.toResponse(updatedUser);
        } catch (Exception e) {
            String statusCode = e instanceof UserNotFoundException ? "404" :
                    e instanceof EmailAlreadyExistsException ? "400" : "500";
            metricService.buildErrorCounter("updateUser", statusCode).increment();
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        metricService.buildApiCounter("deleteUser", "204").increment();

        try {
            if (!userRepository.existsById(id)) {
                metricService.buildErrorCounter("deleteUser", "404").increment();
                throw new UserNotFoundException("User not found with id: " + id);
            }
            userRepository.deleteById(id);
        } catch (Exception e) {
            metricService.buildErrorCounter("deleteUser", "500").increment();
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        metricService.buildApiCounter("getAllUsers", "200").increment();

        try {
            return userRepository.findAll().stream()
                    .map(userMapper::toResponse)
                    .toList();
        } catch (Exception e) {
            metricService.buildErrorCounter("getAllUsers", "500").increment();
            throw e;
        }
    }

    private String generateRandomPassword() {
        // Генерация случайного пароля длиной 12 символов
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
}