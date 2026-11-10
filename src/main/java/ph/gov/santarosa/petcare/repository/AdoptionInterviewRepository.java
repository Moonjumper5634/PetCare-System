package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.AdoptionInterview;

public interface AdoptionInterviewRepository extends JpaRepository<AdoptionInterview, Long> {
    Optional<AdoptionInterview> findByAdoptionId(String adoptionId);
    List<AdoptionInterview> findByAdoptionIdIn(Collection<String> adoptionIds);
}
