package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Row of table notification. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "user_id", nullable = false, length = 20)
    private String userId;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "body", nullable = false, length = 1000)
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false)
    private Boolean read;

    @Column(name = "email_status", nullable = false, length = 20)
    private String emailStatus;

    @Column(name = "email_attempts", nullable = false)
    private Integer emailAttempts;

    @Column(name = "email_last_attempt_at")
    private LocalDateTime emailLastAttemptAt;

    @Column(name = "email_error", length = 500)
    private String emailError;

    public Notification() { }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public String getEmailStatus() { return emailStatus; }
    public void setEmailStatus(String emailStatus) { this.emailStatus = emailStatus; }
    public Integer getEmailAttempts() { return emailAttempts; }
    public void setEmailAttempts(Integer emailAttempts) { this.emailAttempts = emailAttempts; }
    public LocalDateTime getEmailLastAttemptAt() { return emailLastAttemptAt; }
    public void setEmailLastAttemptAt(LocalDateTime emailLastAttemptAt) { this.emailLastAttemptAt = emailLastAttemptAt; }
    public String getEmailError() { return emailError; }
    public void setEmailError(String emailError) { this.emailError = emailError; }
}
