package ru.otus.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.otus.billing.dto.AccountResponse;
import ru.otus.billing.dto.CreateAccountRequest;
import ru.otus.billing.dto.DepositRequest;
import ru.otus.billing.dto.WithdrawRequest;
import ru.otus.billing.service.AccountService;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Billing API")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create account for user")
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get account by user ID")
    public AccountResponse getAccount(@PathVariable Long userId) {
        return accountService.getAccountByUserId(userId);
    }

    @PostMapping("/{userId}/deposit")
    @Operation(summary = "Deposit money")
    public AccountResponse deposit(@PathVariable Long userId, @Valid @RequestBody DepositRequest request) {
        return accountService.deposit(userId, request);
    }

    @PostMapping("/{userId}/withdraw")
    @Operation(summary = "Withdraw money")
    public AccountResponse withdraw(@PathVariable Long userId, @Valid @RequestBody WithdrawRequest request) {
        return accountService.withdraw(userId, request);
    }
}