INSERT INTO id_sequence (kind,prefix,next_value) VALUES ('surrender','SUR-',1001),('shelter','S-',1001);
CREATE TABLE surrender_request (
 id VARCHAR(20) PRIMARY KEY, owner_id VARCHAR(20) NOT NULL REFERENCES app_user(id), pet_id VARCHAR(20) NOT NULL REFERENCES pet(id),
 shelter_pet_id VARCHAR(20) REFERENCES shelter_pet(id), status VARCHAR(30) NOT NULL, reason VARCHAR(1000) NOT NULL,
 circumstances VARCHAR(1000), contact VARCHAR(30) NOT NULL, submitted_date DATE NOT NULL, staff_notes VARCHAR(2000),
 reviewed_by VARCHAR(20) REFERENCES app_user(id), reviewed_at TIMESTAMP(6), handover_date DATE, source VARCHAR(20) NOT NULL
);
CREATE INDEX idx_surrender_owner ON surrender_request(owner_id);
CREATE INDEX idx_surrender_status ON surrender_request(status,submitted_date);
