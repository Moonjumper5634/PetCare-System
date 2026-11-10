package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** Row of table eligibility_assessment. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "eligibility_assessment")
public class EligibilityAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "request_id", nullable = false, length = 20)
    private String requestId;

    @Column(name = "classification", nullable = false, length = 20)
    private String classification;

    @Column(name = "assessed_date", nullable = false)
    private LocalDate assessedDate;

    @Column(name = "assessed_by", length = 20)
    private String assessedBy;

    public EligibilityAssessment() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
    public LocalDate getAssessedDate() { return assessedDate; }
    public void setAssessedDate(LocalDate assessedDate) { this.assessedDate = assessedDate; }
    public String getAssessedBy() { return assessedBy; }
    public void setAssessedBy(String assessedBy) { this.assessedBy = assessedBy; }
}
