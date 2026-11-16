package ph.gov.santarosa.petcare.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.SurrenderRequest;

public interface SurrenderRequestRepository extends JpaRepository<SurrenderRequest,String> {
    List<SurrenderRequest> findByOwnerId(String ownerId);
    boolean existsByPetIdAndStatusIn(String petId, List<String> statuses);
}
