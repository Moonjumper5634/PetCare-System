package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** Row of table adoption_interview. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "adoption_interview")
public class AdoptionInterview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "adoption_id", nullable = false, length = 20)
    private String adoptionId;

    @Column(name = "interview_date", nullable = false)
    private LocalDate interviewDate;

    @Column(name = "staff_name", nullable = false, length = 120)
    private String staffName;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "recommendation", length = 40)
    private String recommendation;

    @Column(name = "checklist", length = 1000)
    private String checklist;

    public AdoptionInterview() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAdoptionId() { return adoptionId; }
    public void setAdoptionId(String adoptionId) { this.adoptionId = adoptionId; }
    public LocalDate getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDate interviewDate) { this.interviewDate = interviewDate; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public String getChecklist() { return checklist; }
    public void setChecklist(String checklist) { this.checklist = checklist; }
}
