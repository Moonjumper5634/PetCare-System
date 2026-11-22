UPDATE service_record SET service_location='Not recorded (legacy)' WHERE service_location IS NULL OR TRIM(service_location)='';
ALTER TABLE service_record MODIFY service_location VARCHAR(160) NOT NULL;
