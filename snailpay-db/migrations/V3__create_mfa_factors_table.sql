-- Requires PostgreSQL 18+ for native uuidv7(). On <18, replace defaults with
-- an application-generated UUIDv7 or a uuidv7_compat() shim function.

CREATE TABLE mfa_factors
(
    id               UUID PRIMARY KEY     DEFAULT uuidv7(),
    customer_id      UUID        NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    type             TEXT        NOT NULL CHECK (type IN ('sms', 'totp', 'email')),
    secret_encrypted BYTEA       NOT NULL,
    phone_number     TEXT,
    is_primary       BOOLEAN     NOT NULL DEFAULT FALSE,
    verified_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_mfa_factors_customer_id ON mfa_factors (customer_id);

-- Enforce at most one primary MFA factor per customer.
CREATE UNIQUE INDEX uq_mfa_factors_primary_per_customer
    ON mfa_factors (customer_id)
    WHERE is_primary = TRUE;