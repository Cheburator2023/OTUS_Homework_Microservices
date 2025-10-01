package ru.otus.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.otus.user.model.User;
import ru.otus.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;

    public boolean isOwner(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();
        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(currentUsername))
                .orElse(false);
    }

    public Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean canAccessUserProfile(Long userId) {
        return isOwner(userId);
    }
}