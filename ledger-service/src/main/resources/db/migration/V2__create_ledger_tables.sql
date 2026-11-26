CREATE TABLE ledger_service.journal_entries (
    journal_id UUID PRIMARY KEY,
    payment_reference VARCHAR(80) NOT NULL UNIQUE,
    currency CHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE ledger_service.ledger_entries (
    entry_id UUID PRIMARY KEY,
    journal_id UUID NOT NULL REFERENCES ledger_service.journal_entries(journal_id),
    account_id VARCHAR(64) NOT NULL,
    debit NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (debit >= 0),
    credit NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (credit >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK ((debit > 0 AND credit = 0) OR (credit > 0 AND debit = 0))
);
