package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Row of table service_request. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "service_request")
public class ServiceRequest {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "pet_id", nullable = false, length = 20)
    private String petId;

    @Column(name = "owner_id", nullable = false, length = 20)
    private String ownerId;

    @Column(name = "service_name", nullable = false, length = 40)
    private String serviceName;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "submitted_date", nullable = false)
    private LocalDate submittedDate;

    @Column(name = "source", nullable = false, length = 10)
    private String source;

    @Column(name = "staff_decision", nullable = false, length = 30)
    private String staffDecision;

    @Column(name = "decision_date")
    private LocalDate decisionDate;

    @Column(name = "decided_by", length = 20)
    private String decidedBy;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public ServiceRequest() { }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDate getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(LocalDate submittedDate) { this.submittedDate = submittedDate; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getStaffDecision() { return staffDecision; }
    public void setStaffDecision(String staffDecision) { this.staffDecision = staffDecision; }
    public LocalDate getDecisionDate() { return decisionDate; }
    public void setDecisionDate(LocalDate decisionDate) { this.decisionDate = decisionDate; }
    public String getDecidedBy() { return decidedBy; }
    public void setDecidedBy(String decidedBy) { this.decidedBy = decidedBy; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
