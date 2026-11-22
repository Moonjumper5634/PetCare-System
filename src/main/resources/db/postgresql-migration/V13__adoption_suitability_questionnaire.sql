ALTER TABLE adoption_application
  ADD COLUMN household_consent BOOLEAN,
  ADD COLUMN children_pets VARCHAR(1000),
  ADD COLUMN secure_area BOOLEAN,
  ADD COLUMN keeping_arrangement VARCHAR(80),
  ADD COLUMN daily_care VARCHAR(1000),
  ADD COLUMN financial_ready BOOLEAN,
  ADD COLUMN landlord_permission VARCHAR(30),
  ADD COLUMN home_assessment BOOLEAN;
