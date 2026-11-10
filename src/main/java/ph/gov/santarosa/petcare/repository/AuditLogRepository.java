package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findByModuleAndRefIdOrderByOccurredAtDesc(String module, String refId);
    List<AuditLog> findTop500ByOrderByOccurredAtDesc();
}
