package com.payflow.paymentservice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "payments", schema = "payment_service")
public class Payment {
    public static final String PENDING = "PENDING";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";

    @Id private UUID paymentId;
    @Column(nullable=false)
    private String fromAccount;

    @Column(nullable=false)
    private String toAccount;

    @Column(nullable=false, precision=19, scale=2)
    private BigDecimal amount;

    @Column(nullable=false, length=3)
    private String currency;

    @Column(nullable=false)
    private String status;

    private String failureReason;

    @Column(nullable=false)
    private Instant createdAt;

    @Column(nullable=false)
    private Instant updatedAt;

    protected Payment() {}
    public Payment(String from, String to, BigDecimal amount, String currency) {
        this.paymentId = UUID.randomUUID();
        this.fromAccount = from; this.toAccount = to;
        this.amount = amount; this.currency = currency;
        this.status = PENDING; this.createdAt = Instant.now(); this.updatedAt = createdAt;
    }

    public void markCompleted() {
        status = COMPLETED;
        failureReason = null;
        updatedAt = Instant.now();
    }

    public void markFailed(String reason) {
        status = FAILED;
        failureReason = reason == null ? null : reason.substring(0, Math.min(reason.length(), 500));
        updatedAt = Instant.now();
    }

    public UUID getPaymentId() { return paymentId; }
    public String getFromAccount() { return fromAccount; }
    public String getToAccount() { return toAccount; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
