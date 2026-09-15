CREATE TABLE customer_addresses
(
    id             UUID PRIMARY KEY     DEFAULT uuidv7(),
    customer_id    UUID        NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    address_type   TEXT        NOT NULL CHECK (address_type IN ('residential', 'mailing')),
    line1          TEXT        NOT NULL,
    line2          TEXT,
    city           TEXT        NOT NULL,
    state_province TEXT        NOT NULL,
    postal_code    TEXT        NOT NULL,
    country_code   CHAR(2)     NOT NULL DEFAULT 'US',
    is_current     BOOLEAN     NOT NULL DEFAULT TRUE,
    superseded_at  TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_customer_addresses_customer_id ON customer_addresses (customer_id);

CREATE UNIQUE INDEX uq_customer_addresses_current
    ON customer_addresses (customer_id, address_type)
    WHERE is_current = TRUE;