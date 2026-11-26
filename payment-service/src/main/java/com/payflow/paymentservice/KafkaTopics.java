package com.payflow.paymentservice;

public final class KafkaTopics {
    private KafkaTopics() {}
    public static final String PAYMENT_INITIATED = "payment.initiated";
    public static final String FUNDS_RESERVED = "funds.reserved";
    public static final String BANK_PAYMENT_REQUESTED = "bank.payment.requested";
    public static final String BANK_PAYMENT_COMPLETED = "bank.payment.completed";
    public static final String LEDGER_POSTED = "ledger.posted";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_RETRY = "payment.retry";
    public static final String PAYMENT_DLQ = "payment.dlq";
}
