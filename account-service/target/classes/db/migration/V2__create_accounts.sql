CREATE TABLE account_service.accounts (
    account_id VARCHAR(64) PRIMARY KEY,
    owner_name VARCHAR(120) NOT NULL,
    available_balance NUMERIC(19,2) NOT NULL CHECK (available_balance >= 0),
    reserved_balance NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (reserved_balance >= 0),
    currency VARCHAR(3) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
