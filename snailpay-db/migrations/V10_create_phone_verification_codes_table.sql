CREATE TABLE phone_verification_codes
(
    id          UUID PRIMARY KEY     DEFAULT uuidv7(),
    customer_id UUID        NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    phone       VARCHAR(20) NOT NULL,
    code_hash   CHAR(64)    NOT NULL,
    attempts    SMALLINT    NOT NULL DEFAULT 0,
    expires_at  TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT phone_verification_codes_attempts_check CHECK (attempts >= 0)
);

CREATE UNIQUE INDEX uq_phone_verification_codes_active
    ON phone_verification_codes (customer_id)
    WHERE consumed_at IS NULL;

CREATE INDEX idx_phone_verification_codes_customer_created
    ON phone_verification_codes (customer_id, created_at DESC);