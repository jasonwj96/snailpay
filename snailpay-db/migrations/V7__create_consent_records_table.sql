CREATE TABLE consent_records
(
    id               UUID PRIMARY KEY     DEFAULT uuidv7(),
    customer_id      UUID        NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    consent_type     TEXT        NOT NULL CHECK (
        consent_type IN ('terms_of_service', 'privacy_policy', 'esign_consent', 'marketing_opt_in')
        ),
    document_version TEXT        NOT NULL,
    accepted_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    ip_address       INET,
    user_agent       TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_consent_records_customer_id ON consent_records (customer_id);
CREATE INDEX idx_consent_records_customer_type ON consent_records (customer_id, consent_type);
