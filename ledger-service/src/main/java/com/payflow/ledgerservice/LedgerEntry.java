package com.payflow.ledgerservice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "ledger_entries", schema = "ledger_service")
public class LedgerEntry {
    @Id
    @Column(name = "entry_id")
    private UUID entryId;
    @Column(name = "journal_id", nullable = false)
    private UUID journalId;
    @Column(name = "account_id", nullable = false)
    private String accountId;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal debit;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal credit;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LedgerEntry() {}

    public LedgerEntry(UUID journalId, String accountId, BigDecimal debit, BigDecimal credit) {
        this.entryId = UUID.randomUUID();
        this.journalId = journalId;
        this.accountId = accountId;
        this.debit = debit;
        this.credit = credit;
        this.createdAt = Instant.now();
    }
    public UUID getEntryId() { return entryId; }
    public UUID getJournalId() { return journalId; }
    public String getAccountId() { return accountId; }
    public BigDecimal getDebit() { return debit; }
    public BigDecimal getCredit() { return credit; }
    public Instant getCreatedAt() { return createdAt; }
}
