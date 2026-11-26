package com.payflow.paymentservice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "payments", schema = "payment_service")
public class Payment {
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

    @Column(nullable=false)
    private Instant createdAt;

    protected Payment() {}
    public Payment(String from, String to, BigDecimal amount, String currency) {
        this.paymentId = UUID.randomUUID();
        this.fromAccount = from; this.toAccount = to;
        this.amount = amount; this.currency = currency;
        this.status = "PENDING"; this.createdAt = Instant.now();
    }
    public UUID getPaymentId() { return paymentId; }
    public String getStatus() { return status; }
}
