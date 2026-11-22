INSERT INTO adoption_application (id,shelter_pet_id,applicant_owner_id,applicant_name,contact,email,address,barangay,reason,experience,housing,status,submitted_date)
VALUES ('ADOPT-1001','S-201','U-1001','Maria Santos','0917 123 4567','resident@petcare.local','Purok 3','Balibago','Provide a permanent and caring home.','Previously cared for family dogs.','Owned House','Interview Required','2026-08-15');
UPDATE shelter_pet SET status='Application Pending' WHERE id='S-201';
UPDATE id_sequence SET next_value=1002 WHERE kind='adopt' AND next_value<1002;
