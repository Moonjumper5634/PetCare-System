package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.EligibilityCriterion;

public interface EligibilityCriterionRepository extends JpaRepository<EligibilityCriterion, Long> {
    List<EligibilityCriterion> findByServiceNameOrderBySortOrder(String serviceName);
    List<EligibilityCriterion> findByServiceNameAndEnabledTrueOrderBySortOrder(String serviceName);
    Optional<EligibilityCriterion> findByServiceNameAndCriterionKey(String serviceName, String criterionKey);
}
