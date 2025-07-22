package ru.otus.user.service;

import ru.otus.user.dto.UserRequest;
import ru.otus.user.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest userRequest);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserRequest userRequest);
    void deleteUser(Long id);
    List<UserResponse> getAllUsers();
}
