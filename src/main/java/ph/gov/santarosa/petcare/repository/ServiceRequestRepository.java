package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.ServiceRequest;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, String> {
    List<ServiceRequest> findByOwnerId(String ownerId);
    List<ServiceRequest> findByPetId(String petId);
    List<ServiceRequest> findByStaffDecision(String staffDecision);
    boolean existsByPetIdAndServiceNameAndStatusIn(String petId, String serviceName, Collection<String> statuses);
}
