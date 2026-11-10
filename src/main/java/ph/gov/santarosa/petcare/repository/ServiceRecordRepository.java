package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.ServiceRecord;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    Optional<ServiceRecord> findByRequestId(String requestId);
    List<ServiceRecord> findByRequestIdIn(Collection<String> requestIds);
}
