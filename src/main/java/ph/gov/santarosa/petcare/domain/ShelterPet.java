package ph.gov.santarosa.petcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** Row of table shelter_pet. Generated from the schema spec; relationships are plain FK id fields. */
@Entity
@Table(name = "shelter_pet")
public class ShelterPet {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "species", nullable = false, length = 20)
    private String species;

    @Column(name = "breed", length = 80)
    private String breed;

    @Column(name = "sex", nullable = false, length = 10)
    private String sex;

    @Column(name = "age_text", length = 40)
    private String ageText;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "temperament", length = 500)
    private String temperament;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "intake_date")
    private LocalDate intakeDate;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    public ShelterPet() { }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public String getAgeText() { return ageText; }
    public void setAgeText(String ageText) { this.ageText = ageText; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTemperament() { return temperament; }
    public void setTemperament(String temperament) { this.temperament = temperament; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getIntakeDate() { return intakeDate; }
    public void setIntakeDate(LocalDate intakeDate) { this.intakeDate = intakeDate; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
