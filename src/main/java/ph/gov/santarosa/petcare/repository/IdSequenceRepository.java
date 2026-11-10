package ph.gov.santarosa.petcare.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ph.gov.santarosa.petcare.domain.IdSequence;

public interface IdSequenceRepository extends JpaRepository<IdSequence, String> {

    /** SELECT ... FOR UPDATE, so two requests can never be handed the same id. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from IdSequence s where s.kind = :kind")
    Optional<IdSequence> lockByKind(@Param("kind") String kind);
}
