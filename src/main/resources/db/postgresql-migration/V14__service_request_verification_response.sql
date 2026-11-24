ALTER TABLE service_request
  ADD COLUMN verification_instructions VARCHAR(1000),
  ADD COLUMN verification_response VARCHAR(2000),
  ADD COLUMN verification_requested_at TIMESTAMP(6),
  ADD COLUMN verification_responded_at TIMESTAMP(6);
