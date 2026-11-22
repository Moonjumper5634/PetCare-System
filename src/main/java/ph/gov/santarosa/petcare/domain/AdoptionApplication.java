package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** Row of table adoption_application. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "adoption_application")
public class AdoptionApplication {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "shelter_pet_id", nullable = false, length = 20)
    private String shelterPetId;

    @Column(name = "applicant_owner_id", nullable = false, length = 20)
    private String applicantOwnerId;

    @Column(name = "applicant_name", nullable = false, length = 160)
    private String applicantName;

    @Column(name = "contact", length = 40)
    private String contact;

    @Column(name = "email", length = 190)
    private String email;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "barangay", length = 80)
    private String barangay;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "experience", length = 1000)
    private String experience;

    @Column(name = "housing", length = 40)
    private String housing;

    @Column(name="household_consent") private Boolean householdConsent;
    @Column(name="children_pets", length=1000) private String childrenPets;
    @Column(name="secure_area") private Boolean secureArea;
    @Column(name="keeping_arrangement", length=80) private String keepingArrangement;
    @Column(name="daily_care", length=1000) private String dailyCare;
    @Column(name="financial_ready") private Boolean financialReady;
    @Column(name="landlord_permission", length=30) private String landlordPermission;
    @Column(name="home_assessment") private Boolean homeAssessment;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "submitted_date", nullable = false)
    private LocalDate submittedDate;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    public AdoptionApplication() { }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getShelterPetId() { return shelterPetId; }
    public void setShelterPetId(String shelterPetId) { this.shelterPetId = shelterPetId; }
    public String getApplicantOwnerId() { return applicantOwnerId; }
    public void setApplicantOwnerId(String applicantOwnerId) { this.applicantOwnerId = applicantOwnerId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getHousing() { return housing; }
    public void setHousing(String housing) { this.housing = housing; }
    public Boolean getHouseholdConsent(){return householdConsent;} public void setHouseholdConsent(Boolean v){householdConsent=v;}
    public String getChildrenPets(){return childrenPets;} public void setChildrenPets(String v){childrenPets=v;}
    public Boolean getSecureArea(){return secureArea;} public void setSecureArea(Boolean v){secureArea=v;}
    public String getKeepingArrangement(){return keepingArrangement;} public void setKeepingArrangement(String v){keepingArrangement=v;}
    public String getDailyCare(){return dailyCare;} public void setDailyCare(String v){dailyCare=v;}
    public Boolean getFinancialReady(){return financialReady;} public void setFinancialReady(Boolean v){financialReady=v;}
    public String getLandlordPermission(){return landlordPermission;} public void setLandlordPermission(String v){landlordPermission=v;}
    public Boolean getHomeAssessment(){return homeAssessment;} public void setHomeAssessment(Boolean v){homeAssessment=v;}
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(LocalDate submittedDate) { this.submittedDate = submittedDate; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
