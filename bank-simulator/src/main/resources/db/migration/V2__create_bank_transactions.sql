CREATE TABLE bank_simulator.bank_transactions (
    bank_reference UUID PRIMARY KEY,
    payment_reference VARCHAR(80) NOT NULL UNIQUE,
    amount NUMERIC(19,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
