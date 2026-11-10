INSERT INTO veterinary_service (name, sort_order) VALUES
  ('Anti-Rabies Vaccination', 1),
  ('Microchipping', 2),
  ('Kapon / Spay-Neuter', 3);

INSERT INTO eligibility_criterion (service_name, criterion_key, label, description, enabled, sort_order) VALUES
  ('Anti-Rabies Vaccination', 'registered', 'Pet Registration', 'Pet must be registered and verified with the CVO.', TRUE, 1),
  ('Anti-Rabies Vaccination', 'minAge', 'Minimum Applicable Age', 'Pet must be at least 3 months old.', TRUE, 2),
  ('Anti-Rabies Vaccination', 'recentVax', 'Recent Anti-Rabies Vaccination', 'No anti-rabies vaccine administered within the past 12 months.', TRUE, 3),
  ('Anti-Rabies Vaccination', 'medical', 'Medical Restriction', 'CVO Verification — confirmed at the time of vaccination.', TRUE, 4),
  ('Microchipping', 'registered', 'Pet Registration', 'Pet must be registered and verified with the CVO.', TRUE, 1),
  ('Microchipping', 'minAge6', 'Minimum Age', 'Dog must be 6 months old and above (per CVO Microchipping campaign requirement).', TRUE, 2),
  ('Microchipping', 'noChip', 'Existing Microchip', 'No existing microchip number already recorded for the pet.', TRUE, 3),
  ('Microchipping', 'info', 'Required Pet Information', 'Breed and physical description must be on file.', TRUE, 4),
  ('Kapon / Spay-Neuter', 'registered', 'Pet Registered and Verified', 'Pet must be registered and verified with the CVO.', TRUE, 1),
  ('Kapon / Spay-Neuter', 'ageRange', 'Age within CVO Kapon Range', 'Dog must be 5 months to 4 years old, per the supplied CVO Kapon announcement.', TRUE, 2),
  ('Kapon / Spay-Neuter', 'weight', 'Weight Requirement Satisfied', 'Dog must meet the CVO Kapon weight requirement (sample reference: at least 3 kg — verify exact figure against the official announcement).', TRUE, 3),
  ('Kapon / Spay-Neuter', 'healthy', 'Apparently Healthy', 'Dog must be recorded as apparently healthy on the pet profile.', TRUE, 4),
  ('Kapon / Spay-Neuter', 'rabies', 'Anti-Rabies Updated', 'Dog''s anti-rabies vaccination must be on file and updated.', TRUE, 5),
  ('Kapon / Spay-Neuter', 'deworm', 'Deworming Updated', 'Dog''s deworming record must be on file and updated.', TRUE, 6),
  ('Kapon / Spay-Neuter', 'residency', 'Owner Proof of Residency Verified', 'Owner''s proof of residency must be verified by the barangay/CVO.', TRUE, 7);

INSERT INTO id_sequence (kind, prefix, next_value) VALUES
  ('reg', 'REG-', 1003), ('req', 'REQ-', 3303), ('adopt', 'ADOPT-', 1001),
  ('pet', 'P-', 1050), ('user', 'U-', 4001), ('notif', 'N-', 1), ('audit', 'A-', 1);

