CREATE TABLE customers
(
    id                UUID PRIMARY KEY     DEFAULT uuidv7(),
    external_id       TEXT        NOT NULL UNIQUE,
    email             TEXT        NOT NULL UNIQUE,
    email_verified_at TIMESTAMPTZ,
    phone             VARCHAR(20) UNIQUE,
    phone_verified_at TIMESTAMPTZ,
    full_name         VARCHAR(255),
    date_of_birth     DATE,
    status            TEXT        NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'LOCKED', 'CLOSED')),
    kyc_status        TEXT        NOT NULL DEFAULT 'NOT_STARTED'
        CHECK (kyc_status IN ('NOT_STARTED', 'PENDING', 'VERIFIED', 'FAILED', 'REQUIRES_REVIEW')),
    kyc_verified_at   TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_customer_email_lower
    ON customers (lower(email));

CREATE INDEX idx_customer_status_pending
    ON customers (status)
    WHERE status <> 'ACTIVE';

CREATE INDEX idx_customer_kyc_status_open
    ON customers (kyc_status)
    WHERE kyc_status NOT IN ('VERIFIED', 'FAILED');