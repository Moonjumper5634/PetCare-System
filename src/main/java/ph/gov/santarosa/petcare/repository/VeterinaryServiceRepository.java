package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.VeterinaryService;

public interface VeterinaryServiceRepository extends JpaRepository<VeterinaryService, String> {
    List<VeterinaryService> findAllByOrderBySortOrder();
}
