ALTER TABLE adoption_application
  ADD COLUMN household_consent BOOLEAN NULL,
  ADD COLUMN children_pets VARCHAR(1000) NULL,
  ADD COLUMN secure_area BOOLEAN NULL,
  ADD COLUMN keeping_arrangement VARCHAR(80) NULL,
  ADD COLUMN daily_care VARCHAR(1000) NULL,
  ADD COLUMN financial_ready BOOLEAN NULL,
  ADD COLUMN landlord_permission VARCHAR(30) NULL,
  ADD COLUMN home_assessment BOOLEAN NULL;
