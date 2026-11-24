ALTER TABLE service_request
  ADD COLUMN verification_instructions VARCHAR(1000) NULL,
  ADD COLUMN verification_response VARCHAR(2000) NULL,
  ADD COLUMN verification_requested_at DATETIME(6) NULL,
  ADD COLUMN verification_responded_at DATETIME(6) NULL;
