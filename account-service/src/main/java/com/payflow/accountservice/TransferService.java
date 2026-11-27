package com.payflow.accountservice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransferService {
    private final AccountRepository accounts;
    private final RestClient ledger;

    public TransferService(AccountRepository accounts, @Value("${payflow.ledger.url}") String ledgerUrl) {
        this.accounts = accounts;
        this.ledger = RestClient.builder().baseUrl(ledgerUrl).build();
    }

    // Debit + credit + ledger posting commit or roll back together (Milestone 1).
    // Milestone 2 hardens the locking; Milestone 5 replaces the synchronous ledger call with a Saga.
    @Transactional
    public void transfer(String paymentId, String from, String to, BigDecimal amount, String currency) {
        if (from.equals(to)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Sender and receiver must differ");
        }
        // Lock in a stable order so A->B and B->A can't deadlock each other.

        List<String> order = Stream.of(from, to).sorted().toList();
        Map<String, Account> locked = new HashMap<>();
        for (String id : order) locked.put(id, lock(id));   // locks in sorted order
        Account sender = locked.get(from);
        Account receiver = locked.get(to);

        if (!sender.getCurrency().equals(currency) || !receiver.getCurrency().equals(currency)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Currency mismatch");
        }
        try {
            sender.debit(amount);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }
        receiver.credit(amount);

        try {
            ledger.post().uri("/api/ledger/journals").body(Map.of(
                "paymentReference", paymentId,
                "currency", currency,
                "entries", List.of(
                    Map.of("accountId", from, "debit", amount, "credit", BigDecimal.ZERO),
                    Map.of("accountId", to, "debit", BigDecimal.ZERO, "credit", amount))))
                .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ledger posting failed: " + e.getMessage());
        }
    }

    private Account lock(String id) {
        return accounts.findByIdForUpdate(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found: " + id));
    }
}
