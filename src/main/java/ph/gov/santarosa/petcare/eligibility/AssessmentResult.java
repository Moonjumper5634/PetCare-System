package ph.gov.santarosa.petcare.eligibility;

import java.time.LocalDate;
import java.util.List;

/** Mirrors the front end's {eligible, classification, criteria, decisionPath, assessedDate}. */
public record AssessmentResult(boolean eligible, Classification classification, List<CriterionResult> criteria,
                               List<DecisionNode> decisionPath, LocalDate assessedDate) { }
