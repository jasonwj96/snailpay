CREATE TABLE auth_sessions
(
    id          UUID PRIMARY KEY     DEFAULT uuidv7(),
    customer_id UUID        NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    token_hash  TEXT        NOT NULL,
    device_info JSONB,
    ip_address  INET,
    user_agent  TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_auth_sessions_token_hash ON auth_sessions (token_hash);
CREATE INDEX idx_auth_sessions_customer_id ON auth_sessions (customer_id);
CREATE INDEX idx_auth_sessions_expires_at ON auth_sessions (expires_at)
    WHERE revoked_at IS NULL;
