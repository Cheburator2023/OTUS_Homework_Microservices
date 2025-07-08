package ru.otus.user.mapper;

import org.springframework.stereotype.Component;
import ru.otus.user.dto.UserResponse;
import ru.otus.user.model.User;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
