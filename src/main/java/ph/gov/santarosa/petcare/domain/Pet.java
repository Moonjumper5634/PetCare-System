package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Row of table pet. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "pet")
public class Pet {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "owner_id", nullable = false, length = 20)
    private String ownerId;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "species", nullable = false, length = 20)
    private String species;

    @Column(name = "breed", length = 80)
    private String breed;

    @Column(name = "sex", nullable = false, length = 10)
    private String sex;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "color", length = 120)
    private String color;

    @Column(name = "barangay", length = 80)
    private String barangay;

    @Column(name = "registration_status", nullable = false, length = 20)
    private String registrationStatus;

    @Column(name = "registered_date")
    private LocalDate registeredDate;

    @Column(name = "registration_source", nullable = false, length = 10)
    private String registrationSource;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "health_status", length = 30)
    private String healthStatus;

    @Column(name = "health_notes", length = 1000)
    private String healthNotes;

    @Column(name = "rabies_status", length = 20)
    private String rabiesStatus;

    @Column(name = "last_rabies")
    private LocalDate lastRabies;

    @Column(name = "deworming_status", length = 20)
    private String dewormingStatus;

    @Column(name = "last_deworming")
    private LocalDate lastDeworming;

    @Column(name = "proof_of_residency", length = 40)
    private String proofOfResidency;

    @Column(name = "microchip", length = 40)
    private String microchip;

    @Column(name = "microchip_status", nullable = false, length = 20)
    private String microchipStatus;

    @Column(name = "kapon_status", length = 20)
    private String kaponStatus;

    @Column(name = "kapon_date")
    private LocalDate kaponDate;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Pet() { }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }
    public String getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
    public LocalDate getRegisteredDate() { return registeredDate; }
    public void setRegisteredDate(LocalDate registeredDate) { this.registeredDate = registeredDate; }
    public String getRegistrationSource() { return registrationSource; }
    public void setRegistrationSource(String registrationSource) { this.registrationSource = registrationSource; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public String getHealthNotes() { return healthNotes; }
    public void setHealthNotes(String healthNotes) { this.healthNotes = healthNotes; }
    public String getRabiesStatus() { return rabiesStatus; }
    public void setRabiesStatus(String rabiesStatus) { this.rabiesStatus = rabiesStatus; }
    public LocalDate getLastRabies() { return lastRabies; }
    public void setLastRabies(LocalDate lastRabies) { this.lastRabies = lastRabies; }
    public String getDewormingStatus() { return dewormingStatus; }
    public void setDewormingStatus(String dewormingStatus) { this.dewormingStatus = dewormingStatus; }
    public LocalDate getLastDeworming() { return lastDeworming; }
    public void setLastDeworming(LocalDate lastDeworming) { this.lastDeworming = lastDeworming; }
    public String getProofOfResidency() { return proofOfResidency; }
    public void setProofOfResidency(String proofOfResidency) { this.proofOfResidency = proofOfResidency; }
    public String getMicrochip() { return microchip; }
    public void setMicrochip(String microchip) { this.microchip = microchip; }
    public String getMicrochipStatus() { return microchipStatus; }
    public void setMicrochipStatus(String microchipStatus) { this.microchipStatus = microchipStatus; }
    public String getKaponStatus() { return kaponStatus; }
    public void setKaponStatus(String kaponStatus) { this.kaponStatus = kaponStatus; }
    public LocalDate getKaponDate() { return kaponDate; }
    public void setKaponDate(LocalDate kaponDate) { this.kaponDate = kaponDate; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
