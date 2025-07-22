package ru.otus.user.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
