ALTER TABLE service_record
  ADD COLUMN presenter_name VARCHAR(160) NULL,
  ADD COLUMN provider_role VARCHAR(80) NULL,
  ADD COLUMN source_type VARCHAR(40) NOT NULL DEFAULT 'Digital request',
  ADD COLUMN source_reference VARCHAR(120) NULL,
  ADD COLUMN service_location VARCHAR(160) NULL;
