package com.payflow.accountservice;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "accounts", schema = "account_service")
public class Account {
    @Id
    @Column(name = "account_id", length = 64)
    private String accountId;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Column(name = "reserved_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal reservedBalance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency;

    @Version
    private Long version;

    protected Account() {}

    public Account(String accountId, String ownerName, BigDecimal openingBalance, String currency) {
        this.accountId = accountId;
        this.ownerName = ownerName;
        this.availableBalance = openingBalance;
        this.currency = currency;
    }
    public String getAccountId() { return accountId; }
    public String getOwnerName() { return ownerName; }
    public BigDecimal getAvailableBalance() { return availableBalance; }
    public BigDecimal getReservedBalance() { return reservedBalance; }
    public String getCurrency() { return currency; }

    public void debit(BigDecimal amount) {
        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds in " + accountId);
        }
        availableBalance = availableBalance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        availableBalance = availableBalance.add(amount);
    }
}
