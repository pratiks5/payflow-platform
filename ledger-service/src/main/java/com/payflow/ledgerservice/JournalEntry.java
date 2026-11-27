package com.payflow.ledgerservice;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "journal_entries", schema = "ledger_service")
public class JournalEntry {
    @Id
    @Column(name = "journal_id")
    private UUID journalId;
    @Column(name = "payment_reference", nullable = false, unique = true)
    private String paymentReference;
    @Column(nullable = false, length = 3, columnDefinition = "char(3)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency;
    @Column(nullable = false)
    private String status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected JournalEntry() {}

    public JournalEntry(String paymentReference, String currency) {
        this.journalId = UUID.randomUUID();
        this.paymentReference = paymentReference;
        this.currency = currency;
        this.status = "POSTED";
        this.createdAt = Instant.now();
    }
    public UUID getJournalId() { return journalId; }
    public String getPaymentReference() { return paymentReference; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
