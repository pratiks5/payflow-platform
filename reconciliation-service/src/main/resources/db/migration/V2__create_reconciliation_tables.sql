CREATE TABLE reconciliation_service.reconciliation_batches (
    batch_id UUID PRIMARY KEY,
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ
);
CREATE TABLE reconciliation_service.reconciliation_records (
    record_id UUID PRIMARY KEY,
    batch_id UUID NOT NULL REFERENCES reconciliation_service.reconciliation_batches(batch_id),
    payment_reference VARCHAR(80) NOT NULL,
    bank_reference VARCHAR(80),
    match_status VARCHAR(40) NOT NULL,
    difference_amount NUMERIC(19,2),
    details TEXT
);
