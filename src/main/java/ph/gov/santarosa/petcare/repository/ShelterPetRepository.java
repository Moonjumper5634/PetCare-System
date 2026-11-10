package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.ShelterPet;

public interface ShelterPetRepository extends JpaRepository<ShelterPet, String> {
    List<ShelterPet> findByStatus(String status);
}
