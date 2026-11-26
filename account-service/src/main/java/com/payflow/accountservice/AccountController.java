package com.payflow.accountservice;

import java.math.BigDecimal;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountRepository repository;
    public AccountController(AccountRepository repository) { this.repository = repository; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@Valid @RequestBody CreateAccountRequest request) {
        if (repository.existsById(request.accountId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account already exists");
        }
        Account account = repository.save(new Account(
            request.accountId(), request.ownerName(), request.openingBalance(), request.currency()));
        return Map.of("accountId", account.getAccountId(),
            "availableBalance", account.getAvailableBalance(), "currency", account.getCurrency());
    }

    @GetMapping("/{accountId}")
    public Map<String, Object> get(@PathVariable String accountId) {
        Account account = repository.findById(accountId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return Map.of("accountId", account.getAccountId(),
            "availableBalance", account.getAvailableBalance(),
            "reservedBalance", account.getReservedBalance(), "currency", account.getCurrency());
    }

    public record CreateAccountRequest(
        @NotBlank String accountId,
        @NotBlank String ownerName,
        @NotNull @DecimalMin("0.00") BigDecimal openingBalance,
        @NotBlank @Size(min=3, max=3) String currency) {}
}
