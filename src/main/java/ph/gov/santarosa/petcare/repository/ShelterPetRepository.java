package ph.gov.santarosa.petcare.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import ph.gov.santarosa.petcare.domain.ShelterPet;

public interface ShelterPetRepository extends JpaRepository<ShelterPet, String> {
    List<ShelterPet> findByStatus(String status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ShelterPet p where p.id = :id")
    Optional<ShelterPet> findLockedById(@Param("id") String id);
}
