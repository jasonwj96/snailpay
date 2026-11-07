-- Rename to the next free Flyway version in your project before applying.

-- 1) Align existing CHECK constraints with the UPPER_SNAKE_CASE convention and
--    add the two new consent types. Harmless if you already uppercased them.
ALTER TABLE consent_records DROP CONSTRAINT IF EXISTS consent_records_consent_type_check;
UPDATE consent_records SET consent_type = UPPER(consent_type);
ALTER TABLE consent_records
    ADD CONSTRAINT consent_records_consent_type_check
    CHECK (consent_type IN ('TERMS_OF_SERVICE', 'PRIVACY_POLICY', 'ESIGN_CONSENT', 'MARKETING_OPT_IN',
                            'CIP_NOTICE_ACKNOWLEDGED', 'KYC_CERTIFICATION'));

ALTER TABLE customer_addresses DROP CONSTRAINT IF EXISTS customer_addresses_address_type_check;
UPDATE customer_addresses SET address_type = UPPER(address_type);
ALTER TABLE customer_addresses
    ADD CONSTRAINT customer_addresses_address_type_check
    CHECK (address_type IN ('RESIDENTIAL', 'MAILING'));

-- 2) Identity data collected for the Customer Identification Program.
--    Sensitive values are stored as KMS ciphertext. Only the last 4 digits of
--    a TIN are kept in clear, for display. The blind index is an HMAC of the
--    TIN, used only to detect the same TIN on two accounts.
CREATE TABLE kyc_profiles (
    id                    UUID PRIMARY KEY DEFAULT uuidv7(),
    customer_id           UUID        NOT NULL UNIQUE REFERENCES customers (id) ON DELETE CASCADE,
    residency_status      TEXT        NOT NULL CHECK (residency_status IN ('US_CITIZEN', 'US_PERMANENT_RESIDENT', 'NON_US_PERSON')),
    citizenship_country   CHAR(2)     NOT NULL,
    tin_type              TEXT        NOT NULL CHECK (tin_type IN ('SSN', 'ITIN', 'NONE')),
    tin_ciphertext        BYTEA,
    tin_last4             CHAR(4),
    tin_blind_index       CHAR(64),
    id_document_type      TEXT        NOT NULL CHECK (id_document_type IN ('DRIVERS_LICENSE', 'STATE_ID', 'PASSPORT', 'PERMANENT_RESIDENT_CARD')),
    id_number_ciphertext  BYTEA       NOT NULL,
    id_issuing_country    CHAR(2)     NOT NULL,
    id_issuing_state      VARCHAR(2),
    id_expiration_date    DATE        NOT NULL,
    submitted_at          TIMESTAMPTZ NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT kyc_profiles_tin_consistency CHECK (
        (tin_type = 'NONE' AND tin_ciphertext IS NULL AND tin_last4 IS NULL AND tin_blind_index IS NULL)
        OR
        (tin_type <> 'NONE' AND tin_ciphertext IS NOT NULL AND tin_last4 IS NOT NULL AND tin_blind_index IS NOT NULL)
    )
);

-- Not unique on purpose: a duplicate TIN sends the application to manual
-- review instead of failing, so a victim of identity theft isn't locked out.
CREATE INDEX idx_kyc_profiles_tin_blind_index ON kyc_profiles (tin_blind_index) WHERE tin_blind_index IS NOT NULL;

-- 3) Uploaded identity documents. The files live in S3; only the key and
--    metadata are stored here. Rows are never deleted: CIP record retention
--    is 5 years, and replaced documents are marked SUPERSEDED.
CREATE TABLE kyc_documents (
    id             UUID PRIMARY KEY DEFAULT uuidv4(),
    customer_id    UUID        NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    document_type  TEXT        NOT NULL CHECK (document_type IN ('GOVERNMENT_ID_FRONT', 'GOVERNMENT_ID_BACK', 'PROOF_OF_ADDRESS')),
    status         TEXT        NOT NULL CHECK (status IN ('PENDING_UPLOAD', 'UPLOADED', 'REJECTED', 'SUPERSEDED')),
    s3_key         TEXT        NOT NULL UNIQUE,
    content_type   TEXT        NOT NULL,
    size_bytes     BIGINT      NOT NULL CHECK (size_bytes > 0 AND size_bytes <= 10485760),
    uploaded_at    TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_kyc_documents_customer_id ON kyc_documents (customer_id);

-- At most one live document per customer and type.
CREATE UNIQUE INDEX uq_kyc_documents_active
    ON kyc_documents (customer_id, document_type)
    WHERE status IN ('PENDING_UPLOAD', 'UPLOADED');
