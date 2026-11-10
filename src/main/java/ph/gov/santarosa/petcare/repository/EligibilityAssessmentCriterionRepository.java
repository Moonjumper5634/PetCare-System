package ph.gov.santarosa.petcare.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ph.gov.santarosa.petcare.domain.EligibilityAssessmentCriterion;

public interface EligibilityAssessmentCriterionRepository extends JpaRepository<EligibilityAssessmentCriterion, Long> {
    List<EligibilityAssessmentCriterion> findByAssessmentIdOrderBySeq(Long assessmentId);
    List<EligibilityAssessmentCriterion> findByAssessmentIdInOrderBySeq(Collection<Long> assessmentIds);

    /** Bulk delete: executes immediately, so re-inserting rows with the same (assessment_id, seq) is safe. */
    @Modifying
    @Query("delete from EligibilityAssessmentCriterion c where c.assessmentId = :assessmentId")
    void deleteByAssessmentId(@Param("assessmentId") Long assessmentId);
}
