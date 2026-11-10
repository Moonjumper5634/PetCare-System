CREATE TABLE system_setting (
  setting_key VARCHAR(80) PRIMARY KEY,
  setting_value VARCHAR(500) NOT NULL,
  updated_by VARCHAR(20),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER trg_system_setting_updated_at BEFORE UPDATE ON system_setting
  FOR EACH ROW EXECUTE FUNCTION petcare_set_updated_at();

INSERT INTO system_setting (setting_key, setting_value) VALUES
 ('office_name', 'City Veterinary Office — Santa Rosa, Laguna'),
 ('contact_email', 'cvo@santarosalaguna.gov.ph'),
 ('contact_number', '(049) 530-0015'),
 ('status_notifications', 'true'),
 ('vaccination_reminders', 'true'),
 ('monthly_admin_summary', 'false');

