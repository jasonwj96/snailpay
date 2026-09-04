CREATE TABLE customer
(
    id                UUID PRIMARY KEY     DEFAULT uuidv7(),
    public_id         TEXT        NOT NULL UNIQUE,
    email             TEXT        NOT NULL UNIQUE,
    email_verified    BOOLEAN     NOT NULL DEFAULT FALSE,
    email_verified_at TIMESTAMPTZ,
    phone             VARCHAR(20) UNIQUE,
    phone_verified    BOOLEAN     NOT NULL DEFAULT FALSE,
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