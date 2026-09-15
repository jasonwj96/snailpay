CREATE TABLE auth_audit_log
(
    id              UUID PRIMARY KEY     DEFAULT uuidv7(),
    customer_id     UUID        REFERENCES customers (id) ON DELETE SET NULL,
    email_attempted TEXT,
    event_type      TEXT        NOT NULL CHECK (
        event_type IN (
                       'login_success', 'login_failure', 'password_reset_requested',
                       'password_reset_completed', 'mfa_challenge_sent',
                       'mfa_challenge_success', 'mfa_challenge_failure',
                       'account_locked', 'account_unlocked'
            )
        ),
    ip_address      INET,
    user_agent      TEXT,
    failure_reason  TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_auth_audit_log_customer_id ON auth_audit_log (customer_id);
CREATE INDEX idx_auth_audit_log_created_at ON auth_audit_log (created_at);
CREATE INDEX idx_auth_audit_log_event_type ON auth_audit_log (event_type);

CREATE OR REPLACE FUNCTION reject_audit_log_mutation()
    RETURNS TRIGGER AS
$$
BEGIN
    RAISE EXCEPTION 'auth_audit_log is append-only: % is not permitted', TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_auth_audit_log_no_update
    BEFORE UPDATE
    ON auth_audit_log
    FOR EACH ROW
EXECUTE FUNCTION reject_audit_log_mutation();

CREATE TRIGGER trg_auth_audit_log_no_delete
    BEFORE DELETE
    ON auth_audit_log
    FOR EACH ROW
EXECUTE FUNCTION reject_audit_log_mutation();