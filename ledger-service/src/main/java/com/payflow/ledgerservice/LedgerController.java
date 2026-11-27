package com.payflow.ledgerservice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {
    private final JournalEntryRepository journals;
    private final LedgerEntryRepository entries;

    public LedgerController(JournalEntryRepository journals, LedgerEntryRepository entries) {
        this.journals = journals;
        this.entries = entries;
    }

    // Double-entry: total debits must equal total credits. Re-posting the same
    // paymentReference returns the existing journal (UNIQUE constraint backs this up).
    @PostMapping("/journals")
    @Transactional
    public Map<String, Object> post(@Valid @RequestBody PostJournalRequest request) {
        var existing = journals.findByPaymentReference(request.paymentReference());
        if (existing.isPresent()) {
            return Map.of("journalId", existing.get().getJournalId(), "status", existing.get().getStatus(),
                "duplicate", true);
        }
        BigDecimal debits = request.entries().stream().map(EntryLine::debit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credits = request.entries().stream().map(EntryLine::credit).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (debits.compareTo(credits) != 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Unbalanced journal: debits=" + debits + " credits=" + credits);
        }
        for (EntryLine line : request.entries()) {
            boolean debitOnly = line.debit().signum() > 0 && line.credit().signum() == 0;
            boolean creditOnly = line.credit().signum() > 0 && line.debit().signum() == 0;
            if (!debitOnly && !creditOnly) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Each line must have exactly one of debit/credit greater than zero");
            }
        }
        JournalEntry journal = journals.save(new JournalEntry(request.paymentReference(), request.currency()));
        for (EntryLine line : request.entries()) {
            entries.save(new LedgerEntry(journal.getJournalId(), line.accountId(), line.debit(), line.credit()));
        }
        return Map.of("journalId", journal.getJournalId(), "status", journal.getStatus(), "duplicate", false);
    }

    @GetMapping("/entries")
    public List<LedgerEntry> list(@RequestParam(required = false) String accountId) {
        return accountId == null ? entries.findTop100ByOrderByCreatedAtDesc()
            : entries.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @GetMapping("/journals")
    public List<JournalEntry> journals() {
        return journals.findAll();
    }

    public record EntryLine(
        @NotBlank String accountId,
        @NotNull @DecimalMin("0.00") BigDecimal debit,
        @NotNull @DecimalMin("0.00") BigDecimal credit) {}

    public record PostJournalRequest(
        @NotBlank String paymentReference,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotEmpty @Valid List<EntryLine> entries) {}
}
