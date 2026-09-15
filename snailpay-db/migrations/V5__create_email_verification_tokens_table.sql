-- uuidv4() used for the PK here (not uuidv7()) since this is a token-adjacent
-- table: v7's embedded timestamp would leak issuance time if the id ever
-- leaked, and there's no query-pattern benefit to time-ordering here.

CREATE TABLE email_verification_tokens
(
    id          UUID PRIMARY KEY     DEFAULT uuidv4(),
    customer_id UUID        NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    token_hash  TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ NOT NULL,
    verified_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_email_verification_tokens_token_hash
    ON email_verification_tokens (token_hash);
CREATE INDEX idx_email_verification_tokens_customer_id
    ON email_verification_tokens (customer_id);
