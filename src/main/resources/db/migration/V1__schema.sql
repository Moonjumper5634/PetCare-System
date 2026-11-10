-- PetCARE schema (MySQL 8 / MariaDB 10.5+).
-- Derived field-by-field from the PetCARE-dev front end data model (src/data/*, src/services/*).
-- Primary keys keep the front end's own ids (U-1001, P-1042, REG-1002, REQ-3301, ADOPT-1001, S-201,
-- N-1, A-1) so the UI, QR codes and audit references work unchanged. New ids come from id_sequence.

CREATE TABLE id_sequence (
  kind        VARCHAR(10) NOT NULL,
  prefix      VARCHAR(10) NOT NULL,
  next_value  BIGINT      NOT NULL,
  PRIMARY KEY (kind)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE app_user (
  id                   VARCHAR(20)  NOT NULL,
  role                 VARCHAR(10)  NOT NULL,                 -- owner (Resident) | staff | admin
  first_name           VARCHAR(80),
  middle_name          VARCHAR(80),
  last_name            VARCHAR(80),
  full_name            VARCHAR(160) NOT NULL,
  email                VARCHAR(190) NOT NULL,
  mobile               VARCHAR(30),
  house_street         VARCHAR(160),
  barangay             VARCHAR(80),
  city                 VARCHAR(80),
  province             VARCHAR(80),
  password_hash        VARCHAR(100) NOT NULL,                 -- bcrypt; never plaintext
  status               VARCHAR(30)  NOT NULL,
  initials             VARCHAR(4),
  job_title            VARCHAR(80),                           -- front end field "tag"
  must_change_password BOOLEAN      NOT NULL DEFAULT FALSE,
  verification_code    VARCHAR(10),                           -- dev stand-in for an emailed/SMS OTP
  created_at           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uq_app_user_email (email),
  KEY ix_app_user_role (role),
  CONSTRAINT chk_app_user_role   CHECK (role IN ('owner','staff','admin')),
  CONSTRAINT chk_app_user_status CHECK (status IN ('Active','Inactive','Pending Verification'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE veterinary_service (
  name        VARCHAR(40) NOT NULL,                           -- natural key, used everywhere in the UI
  sort_order  INT         NOT NULL DEFAULT 0,
  PRIMARY KEY (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eligibility_criterion (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  service_name   VARCHAR(40)  NOT NULL,
  criterion_key  VARCHAR(30)  NOT NULL,                       -- registered, minAge, recentVax, ...
  label          VARCHAR(120) NOT NULL,
  description    VARCHAR(600) NOT NULL,
  enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
  sort_order     INT          NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_criterion (service_name, criterion_key),
  CONSTRAINT fk_criterion_service FOREIGN KEY (service_name) REFERENCES veterinary_service (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pet (
  id                   VARCHAR(20)   NOT NULL,
  owner_id             VARCHAR(20)   NOT NULL,
  name                 VARCHAR(80)   NOT NULL,
  species              VARCHAR(20)   NOT NULL DEFAULT 'Dog',
  breed                VARCHAR(80),
  sex                  VARCHAR(10)   NOT NULL,
  dob                  DATE          NOT NULL,
  color                VARCHAR(120),
  barangay             VARCHAR(80),
  registration_status  VARCHAR(20)   NOT NULL DEFAULT 'Pending',
  registered_date      DATE,
  registration_source  VARCHAR(10)   NOT NULL DEFAULT 'Owner',
  weight_kg            DECIMAL(5,2),
  health_status        VARCHAR(30),
  health_notes         VARCHAR(1000),
  rabies_status        VARCHAR(20),
  last_rabies          DATE,
  deworming_status     VARCHAR(20),
  last_deworming       DATE,
  proof_of_residency   VARCHAR(40),
  microchip            VARCHAR(40),
  microchip_status     VARCHAR(20)   NOT NULL DEFAULT 'Not Microchipped',
  kapon_status         VARCHAR(20),
  kapon_date           DATE,
  photo_url            VARCHAR(500),
  created_at           DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at           DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY ix_pet_owner (owner_id),
  KEY ix_pet_reg_status (registration_status),
  UNIQUE KEY uq_pet_microchip (microchip),
  CONSTRAINT fk_pet_owner FOREIGN KEY (owner_id) REFERENCES app_user (id),
  CONSTRAINT chk_pet_sex        CHECK (sex IN ('Male','Female')),
  CONSTRAINT chk_pet_reg_status CHECK (registration_status IN ('Pending','Verified','Rejected')),
  CONSTRAINT chk_pet_reg_source CHECK (registration_source IN ('Owner','Walk-in')),
  CONSTRAINT chk_pet_weight     CHECK (weight_kg IS NULL OR weight_kg > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pet_registration (
  id               VARCHAR(20)   NOT NULL,
  pet_id           VARCHAR(20)   NOT NULL,
  owner_id         VARCHAR(20)   NOT NULL,
  status           VARCHAR(20)   NOT NULL DEFAULT 'Pending',
  submitted_date   DATE          NOT NULL,
  source           VARCHAR(10)   NOT NULL DEFAULT 'Owner',
  rejection_reason VARCHAR(1000),
  reviewed_by      VARCHAR(20),
  reviewed_at      DATETIME(6),
  PRIMARY KEY (id),
  KEY ix_registration_pet (pet_id),
  KEY ix_registration_status (status),
  CONSTRAINT fk_registration_pet   FOREIGN KEY (pet_id)      REFERENCES pet (id),
  CONSTRAINT fk_registration_owner FOREIGN KEY (owner_id)    REFERENCES app_user (id),
  CONSTRAINT fk_registration_staff FOREIGN KEY (reviewed_by) REFERENCES app_user (id),
  CONSTRAINT chk_registration_status CHECK (status IN ('Pending','Verified','Rejected')),
  CONSTRAINT chk_registration_source CHECK (source IN ('Owner','Walk-in'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE service_request (
  id               VARCHAR(20)   NOT NULL,
  pet_id           VARCHAR(20)   NOT NULL,
  owner_id         VARCHAR(20)   NOT NULL,
  service_name     VARCHAR(40)   NOT NULL,
  remarks          VARCHAR(1000),
  submitted_date   DATE          NOT NULL,
  source           VARCHAR(10)   NOT NULL DEFAULT 'Owner',    -- Owner | Walk-in | QR Scan
  staff_decision   VARCHAR(30)   NOT NULL DEFAULT 'Pending',  -- the staff's FINAL decision, separate from the system's classification
  decision_date    DATE,
  decided_by       VARCHAR(20),
  rejection_reason VARCHAR(1000),
  status           VARCHAR(20)   NOT NULL DEFAULT 'Submitted',
  created_at       DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at       DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY ix_request_pet (pet_id),
  KEY ix_request_owner (owner_id),
  KEY ix_request_decision (staff_decision, status),
  CONSTRAINT fk_request_pet     FOREIGN KEY (pet_id)       REFERENCES pet (id),
  CONSTRAINT fk_request_owner   FOREIGN KEY (owner_id)     REFERENCES app_user (id),
  CONSTRAINT fk_request_service FOREIGN KEY (service_name) REFERENCES veterinary_service (name),
  CONSTRAINT fk_request_staff   FOREIGN KEY (decided_by)   REFERENCES app_user (id),
  CONSTRAINT chk_request_source   CHECK (source IN ('Owner','Walk-in','QR Scan')),
  CONSTRAINT chk_request_decision CHECK (staff_decision IN ('Pending','Approved','Rejected','Verification Requested')),
  CONSTRAINT chk_request_status   CHECK (status IN ('Submitted','For Review','Approved','Rejected','Completed'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- The system's decision-tree result for a request. The decisionPath the UI draws is derived from the
-- criteria rows + classification, so it is not stored.
CREATE TABLE eligibility_assessment (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  request_id      VARCHAR(20)  NOT NULL,
  classification  VARCHAR(20)  NOT NULL,
  assessed_date   DATE         NOT NULL,
  assessed_by     VARCHAR(20),
  PRIMARY KEY (id),
  UNIQUE KEY uq_assessment_request (request_id),
  CONSTRAINT fk_assessment_request FOREIGN KEY (request_id)  REFERENCES service_request (id),
  CONSTRAINT fk_assessment_staff   FOREIGN KEY (assessed_by) REFERENCES app_user (id),
  CONSTRAINT chk_assessment_class  CHECK (classification IN ('ELIGIBLE','FOR REVIEW','NOT ELIGIBLE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eligibility_assessment_criterion (
  id             BIGINT        NOT NULL AUTO_INCREMENT,
  assessment_id  BIGINT        NOT NULL,
  seq            INT           NOT NULL,
  label          VARCHAR(160)  NOT NULL,
  actual_value   VARCHAR(200),
  result         VARCHAR(10)   NOT NULL,
  basis          VARCHAR(600),
  PRIMARY KEY (id),
  UNIQUE KEY uq_assessment_seq (assessment_id, seq),
  CONSTRAINT fk_acrit_assessment FOREIGN KEY (assessment_id) REFERENCES eligibility_assessment (id) ON DELETE CASCADE,
  CONSTRAINT chk_acrit_result CHECK (result IN ('PASS','FAIL','REVIEW'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE service_record (
  id              BIGINT        NOT NULL AUTO_INCREMENT,
  request_id      VARCHAR(20)   NOT NULL,
  record_date     DATE          NOT NULL,
  staff_name      VARCHAR(120)  NOT NULL,
  remarks         VARCHAR(1000),
  vaccine_type    VARCHAR(80),
  batch_number    VARCHAR(60),
  next_due        DATE,
  microchip_number VARCHAR(40),
  procedure_date  DATE,
  post_remarks    VARCHAR(1000),
  PRIMARY KEY (id),
  UNIQUE KEY uq_record_request (request_id),
  CONSTRAINT fk_record_request FOREIGN KEY (request_id) REFERENCES service_request (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shelter_pet (
  id           VARCHAR(20)   NOT NULL,
  name         VARCHAR(80)   NOT NULL,
  species      VARCHAR(20)   NOT NULL DEFAULT 'Dog',
  breed        VARCHAR(80),
  sex          VARCHAR(10)   NOT NULL,
  age_text     VARCHAR(40),                                    -- free text in the UI ("~1 year")
  description  VARCHAR(1000),
  temperament  VARCHAR(500),
  status       VARCHAR(30)   NOT NULL DEFAULT 'Available',
  intake_date  DATE,
  photo_url    VARCHAR(500),
  PRIMARY KEY (id),
  KEY ix_shelter_status (status),
  CONSTRAINT chk_shelter_sex    CHECK (sex IN ('Male','Female')),
  CONSTRAINT chk_shelter_status CHECK (status IN ('Available','Application Pending','Adopted'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE adoption_application (
  id                 VARCHAR(20)   NOT NULL,
  shelter_pet_id     VARCHAR(20)   NOT NULL,
  applicant_owner_id VARCHAR(20)   NOT NULL,
  applicant_name     VARCHAR(160)  NOT NULL,
  contact            VARCHAR(40),
  email              VARCHAR(190),
  address            VARCHAR(255),
  barangay           VARCHAR(80),
  reason             VARCHAR(1000),
  experience         VARCHAR(1000),
  housing            VARCHAR(40),
  status             VARCHAR(30)   NOT NULL DEFAULT 'Interview Required',
  submitted_date     DATE          NOT NULL,
  rejection_reason   VARCHAR(1000),
  PRIMARY KEY (id),
  KEY ix_adoption_pet (shelter_pet_id),
  KEY ix_adoption_applicant (applicant_owner_id),
  CONSTRAINT fk_adoption_pet       FOREIGN KEY (shelter_pet_id)     REFERENCES shelter_pet (id),
  CONSTRAINT fk_adoption_applicant FOREIGN KEY (applicant_owner_id) REFERENCES app_user (id),
  CONSTRAINT chk_adoption_status CHECK (status IN ('Interview Required','Interview Completed','Approved','Rejected'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE adoption_interview (
  id              BIGINT        NOT NULL AUTO_INCREMENT,
  adoption_id     VARCHAR(20)   NOT NULL,
  interview_date  DATE          NOT NULL,
  staff_name      VARCHAR(120)  NOT NULL,
  notes           VARCHAR(2000),
  recommendation  VARCHAR(40),
  checklist       VARCHAR(1000),                               -- JSON array of ticked items, as sent by the UI
  PRIMARY KEY (id),
  UNIQUE KEY uq_interview_adoption (adoption_id),
  CONSTRAINT fk_interview_adoption FOREIGN KEY (adoption_id) REFERENCES adoption_application (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification (
  id          VARCHAR(20)   NOT NULL,
  user_id     VARCHAR(20)   NOT NULL,
  title       VARCHAR(160)  NOT NULL,
  body        VARCHAR(1000) NOT NULL,
  created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  is_read     BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id),
  KEY ix_notification_user (user_id, is_read),
  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_log (
  id           VARCHAR(20)   NOT NULL,
  occurred_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  user_id      VARCHAR(20),                                    -- nullable: pre-login events, deleted users
  user_name    VARCHAR(160)  NOT NULL,                         -- name as shown in the UI at that moment
  action       VARCHAR(40)   NOT NULL,
  module       VARCHAR(60)   NOT NULL,
  ref_id       VARCHAR(40),
  description  VARCHAR(1000),
  PRIMARY KEY (id),
  KEY ix_audit_time (occurred_at),
  KEY ix_audit_module_ref (module, ref_id),
  CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
