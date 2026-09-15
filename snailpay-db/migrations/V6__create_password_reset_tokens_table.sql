-- Same reasoning as email_verification_tokens: uuidv4() to avoid leaking a
-- creation timestamp on a security-sensitive, single-use token.

CREATE TABLE password_reset_tokens
(
    id          UUID PRIMARY KEY     DEFAULT uuidv4(),
    customer_id UUID        NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    token_hash  TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_password_reset_tokens_token_hash
    ON password_reset_tokens (token_hash);
CREATE INDEX idx_password_reset_tokens_customer_id
    ON password_reset_tokens (customer_id);
