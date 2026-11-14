ALTER TABLE eligibility_assessment
    ADD COLUMN ruleset_version VARCHAR(64) NOT NULL DEFAULT 'legacy';

ALTER TABLE notification
    ADD COLUMN email_status VARCHAR(20) NOT NULL DEFAULT 'Not Requested',
    ADD COLUMN email_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN email_last_attempt_at TIMESTAMP NULL,
    ADD COLUMN email_error VARCHAR(500) NULL;

CREATE INDEX idx_notification_email_retry
    ON notification (email_status, email_attempts, created_at);
