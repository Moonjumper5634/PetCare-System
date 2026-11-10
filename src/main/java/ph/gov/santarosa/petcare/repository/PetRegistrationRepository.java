package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.PetRegistration;

public interface PetRegistrationRepository extends JpaRepository<PetRegistration, String> {
    List<PetRegistration> findByStatus(String status);
    List<PetRegistration> findByPetId(String petId);
    List<PetRegistration> findByOwnerId(String ownerId);
}
