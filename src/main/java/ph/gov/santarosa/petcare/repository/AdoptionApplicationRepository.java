package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.AdoptionApplication;

public interface AdoptionApplicationRepository extends JpaRepository<AdoptionApplication, String> {
    List<AdoptionApplication> findByApplicantOwnerId(String applicantOwnerId);
    List<AdoptionApplication> findByShelterPetId(String shelterPetId);
}
