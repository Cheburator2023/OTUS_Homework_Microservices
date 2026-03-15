package ru.otus.billing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.billing.dto.AccountResponse;
import ru.otus.billing.dto.CreateAccountRequest;
import ru.otus.billing.dto.DepositRequest;
import ru.otus.billing.dto.WithdrawRequest;
import ru.otus.billing.exception.AccountNotFoundException;
import ru.otus.billing.exception.InsufficientFundsException;
import ru.otus.billing.model.Account;
import ru.otus.billing.repository.AccountRepository;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (accountRepository.existsByUserId(request.userId())) {
            throw new RuntimeException("Account already exists for user: " + request.userId());
        }
        Account account = new Account(request.userId());
        account = accountRepository.save(account);
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByUserId(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for user: " + userId));
        return toResponse(account);
    }

    @Transactional
    public AccountResponse deposit(Long userId, DepositRequest request) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for user: " + userId));
        account.setBalance(account.getBalance().add(request.amount()));
        account = accountRepository.save(account);
        return toResponse(account);
    }

    @Transactional
    public AccountResponse withdraw(Long userId, WithdrawRequest request) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for user: " + userId));
        if (account.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Required: " + request.amount() + ", available: " + account.getBalance());
        }
        account.setBalance(account.getBalance().subtract(request.amount()));
        account = accountRepository.save(account);
        return toResponse(account);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getUserId(), account.getBalance());
    }
}