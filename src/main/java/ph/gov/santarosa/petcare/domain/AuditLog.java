package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Row of table audit_log. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "user_id", length = 20)
    private String userId;

    @Column(name = "user_name", nullable = false, length = 160)
    private String userName;

    @Column(name = "action", nullable = false, length = 40)
    private String action;

    @Column(name = "module", nullable = false, length = 60)
    private String module;

    @Column(name = "ref_id", length = 40)
    private String refId;

    @Column(name = "description", length = 1000)
    private String description;

    public AuditLog() { }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getRefId() { return refId; }
    public void setRefId(String refId) { this.refId = refId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
