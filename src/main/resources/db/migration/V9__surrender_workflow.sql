INSERT INTO id_sequence (kind,prefix,next_value) VALUES ('surrender','SUR-',1001),('shelter','S-',1001);
CREATE TABLE surrender_request (
 id VARCHAR(20) PRIMARY KEY, owner_id VARCHAR(20) NOT NULL, pet_id VARCHAR(20) NOT NULL, shelter_pet_id VARCHAR(20),
 status VARCHAR(30) NOT NULL, reason VARCHAR(1000) NOT NULL, circumstances VARCHAR(1000), contact VARCHAR(30) NOT NULL,
 submitted_date DATE NOT NULL, staff_notes VARCHAR(2000), reviewed_by VARCHAR(20), reviewed_at DATETIME(6), handover_date DATE,
 source VARCHAR(20) NOT NULL,
 CONSTRAINT fk_surrender_owner FOREIGN KEY(owner_id) REFERENCES app_user(id),
 CONSTRAINT fk_surrender_pet FOREIGN KEY(pet_id) REFERENCES pet(id),
 CONSTRAINT fk_surrender_shelter FOREIGN KEY(shelter_pet_id) REFERENCES shelter_pet(id),
 CONSTRAINT fk_surrender_reviewer FOREIGN KEY(reviewed_by) REFERENCES app_user(id)
);
CREATE INDEX idx_surrender_owner ON surrender_request(owner_id);
CREATE INDEX idx_surrender_status ON surrender_request(status,submitted_date);
