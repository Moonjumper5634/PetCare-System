ALTER TABLE shelter_pet DROP CONSTRAINT chk_shelter_status;
ALTER TABLE shelter_pet ADD CONSTRAINT chk_shelter_status CHECK (status IN ('Available','Application Pending','Adopted','Unavailable','Archived','Under Assessment'));
