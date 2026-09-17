CREATE TABLE auth_credentials
(
    id                    UUID PRIMARY KEY     DEFAULT uuidv7(),
    customer_id           UUID        NOT NULL
        REFERENCES customers (id) ON DELETE CASCADE,
    password_hash         TEXT        NOT NULL,
    password_algo         TEXT        NOT NULL DEFAULT 'ARGON2ID'
        CHECK (password_algo IN ('ARGON2ID')),
    password_updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    failed_login_attempts INTEGER     NOT NULL DEFAULT 0,
    locked_until          TIMESTAMPTZ,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_auth_credentials_customer_id
    ON auth_credentials (customer_id);

CREATE INDEX idx_auth_credentials_locked_until
    ON auth_credentials (locked_until)
    WHERE locked_until IS NOT NULL;