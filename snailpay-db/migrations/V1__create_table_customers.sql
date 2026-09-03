CREATE TABLE customers
(
    id            UUID PRIMARY KEY     DEFAULT uuidv7(),
    email         TEXT UNIQUE NOT NULL,
    phone         TEXT UNIQUE,
    first_name    TEXT        NOT NULL,
    last_name     TEXT        NOT NULL,
    date_of_birth DATE        NOT NULL,
    status        TEXT        NOT NULL DEFAULT 'pending_verification'
        CHECK (status IN ('active',
                          'pending_verification',
                          'suspended',
                          'closed')),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
)