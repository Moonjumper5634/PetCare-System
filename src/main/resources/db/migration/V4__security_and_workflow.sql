-- Phase 3: login protection and expiring one-time codes.
ALTER TABLE app_user
  ADD COLUMN failed_login_attempts   INT         NOT NULL DEFAULT 0,
  ADD COLUMN locked_until            DATETIME(6) NULL,
  ADD COLUMN last_login_at           DATETIME(6) NULL,
  ADD COLUMN verification_purpose    VARCHAR(10) NULL,           -- 'signup' | 'reset'
  ADD COLUMN verification_expires_at DATETIME(6) NULL,
  ADD COLUMN verification_attempts   INT         NOT NULL DEFAULT 0;

ALTER TABLE app_user
  ADD CONSTRAINT chk_app_user_purpose CHECK (verification_purpose IS NULL OR verification_purpose IN ('signup','reset'));

-- Only matters for the demo seed (V3), which ships one pending account with a fixed dev code.
-- On a production database app_user is empty here, so this touches nothing.
UPDATE app_user
   SET verification_purpose = 'signup', verification_expires_at = '2099-12-31 00:00:00'
 WHERE verification_code IS NOT NULL;
