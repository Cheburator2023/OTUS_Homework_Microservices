package ru.otus.user.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.otus.user.dto.UserRequest;
import ru.otus.user.dto.UserResponse;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserResponse createUser(UserRequest userRequest);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserRequest userRequest);
    void deleteUser(Long id);
    List<UserResponse> getAllUsers();
}