package ru.otus.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.billing.model.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}