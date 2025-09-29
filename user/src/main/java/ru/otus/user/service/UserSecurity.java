package ru.otus.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.otus.user.repository.UserRepository;

@Service("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private final UserRepository userRepository;

    public boolean checkUserId(Authentication authentication, Long userId) {
        String username = authentication.getName();
        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(username))
                .orElse(false);
    }

    public Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
                .map(user -> user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}