package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Row of table eligibility_assessment_criterion. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "eligibility_assessment_criterion")
public class EligibilityAssessmentCriterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "assessment_id", nullable = false)
    private Long assessmentId;

    @Column(name = "seq", nullable = false)
    private Integer seq;

    @Column(name = "label", nullable = false, length = 160)
    private String label;

    @Column(name = "actual_value", length = 200)
    private String actualValue;

    @Column(name = "result", nullable = false, length = 10)
    private String result;

    @Column(name = "basis", length = 600)
    private String basis;

    public EligibilityAssessmentCriterion() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }
    public Integer getSeq() { return seq; }
    public void setSeq(Integer seq) { this.seq = seq; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getActualValue() { return actualValue; }
    public void setActualValue(String actualValue) { this.actualValue = actualValue; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getBasis() { return basis; }
    public void setBasis(String basis) { this.basis = basis; }
}
