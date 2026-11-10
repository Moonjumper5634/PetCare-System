ALTER TABLE app_user
  ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0,
  ADD COLUMN locked_until TIMESTAMP(6),
  ADD COLUMN last_login_at TIMESTAMP(6),
  ADD COLUMN verification_purpose VARCHAR(10),
  ADD COLUMN verification_expires_at TIMESTAMP(6),
  ADD COLUMN verification_attempts INTEGER NOT NULL DEFAULT 0;

ALTER TABLE app_user
  ADD CONSTRAINT chk_app_user_purpose
  CHECK (verification_purpose IS NULL OR verification_purpose IN ('signup','reset'));

UPDATE app_user
SET verification_purpose = 'signup', verification_expires_at = TIMESTAMP '2099-12-31 00:00:00'
WHERE verification_code IS NOT NULL;

