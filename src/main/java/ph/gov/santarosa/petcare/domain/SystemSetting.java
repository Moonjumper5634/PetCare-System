package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_setting")
public class SystemSetting {
    @Id @Column(name = "setting_key", length = 80) private String key;
    @Column(name = "setting_value", nullable = false, length = 500) private String value;
    @Column(name = "updated_by", length = 20) private String updatedBy;
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false) private LocalDateTime updatedAt;
    public SystemSetting() { }
    public String getKey() { return key; } public void setKey(String key) { this.key = key; }
    public String getValue() { return value; } public void setValue(String value) { this.value = value; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
