package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.EligibilityAssessment;

public interface EligibilityAssessmentRepository extends JpaRepository<EligibilityAssessment, Long> {
    Optional<EligibilityAssessment> findByRequestId(String requestId);
    List<EligibilityAssessment> findByRequestIdIn(Collection<String> requestIds);
}
