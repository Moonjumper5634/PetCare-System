package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "surrender_request")
public class SurrenderRequest {
    @Id private String id;
    @Column(name="owner_id", nullable=false, length=20) private String ownerId;
    @Column(name="pet_id", nullable=false, length=20) private String petId;
    @Column(name="shelter_pet_id", length=20) private String shelterPetId;
    @Column(nullable=false, length=30) private String status;
    @Column(nullable=false, length=1000) private String reason;
    @Column(length=1000) private String circumstances;
    @Column(nullable=false, length=30) private String contact;
    @Column(name="submitted_date", nullable=false) private LocalDate submittedDate;
    @Column(name="staff_notes", length=2000) private String staffNotes;
    @Column(name="reviewed_by", length=20) private String reviewedBy;
    @Column(name="reviewed_at") private LocalDateTime reviewedAt;
    @Column(name="handover_date") private LocalDate handoverDate;
    @Column(nullable=false, length=20) private String source;

    public String getId(){return id;} public void setId(String v){id=v;}
    public String getOwnerId(){return ownerId;} public void setOwnerId(String v){ownerId=v;}
    public String getPetId(){return petId;} public void setPetId(String v){petId=v;}
    public String getShelterPetId(){return shelterPetId;} public void setShelterPetId(String v){shelterPetId=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
    public String getCircumstances(){return circumstances;} public void setCircumstances(String v){circumstances=v;}
    public String getContact(){return contact;} public void setContact(String v){contact=v;}
    public LocalDate getSubmittedDate(){return submittedDate;} public void setSubmittedDate(LocalDate v){submittedDate=v;}
    public String getStaffNotes(){return staffNotes;} public void setStaffNotes(String v){staffNotes=v;}
    public String getReviewedBy(){return reviewedBy;} public void setReviewedBy(String v){reviewedBy=v;}
    public LocalDateTime getReviewedAt(){return reviewedAt;} public void setReviewedAt(LocalDateTime v){reviewedAt=v;}
    public LocalDate getHandoverDate(){return handoverDate;} public void setHandoverDate(LocalDate v){handoverDate=v;}
    public String getSource(){return source;} public void setSource(String v){source=v;}
}
