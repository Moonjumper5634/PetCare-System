package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.Pet;

public interface PetRepository extends JpaRepository<Pet, String> {
    List<Pet> findByOwnerId(String ownerId);
    List<Pet> findByOwnerIdAndRegistrationStatus(String ownerId, String registrationStatus);
    boolean existsByOwnerIdAndNameIgnoreCaseAndDob(String ownerId, String name, LocalDate dob);
    Optional<Pet> findByOwnerIdAndNameIgnoreCaseAndDob(String ownerId, String name, LocalDate dob);
    Optional<Pet> findByMicrochip(String microchip);
}
