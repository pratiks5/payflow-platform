ALTER TABLE payment_service.payments ADD COLUMN failure_reason VARCHAR(500);
ALTER TABLE payment_service.payments ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
