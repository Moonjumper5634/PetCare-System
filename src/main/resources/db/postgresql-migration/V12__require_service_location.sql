UPDATE service_record SET service_location='Not recorded (legacy)' WHERE service_location IS NULL OR BTRIM(service_location)='';
ALTER TABLE service_record ALTER COLUMN service_location SET NOT NULL;
