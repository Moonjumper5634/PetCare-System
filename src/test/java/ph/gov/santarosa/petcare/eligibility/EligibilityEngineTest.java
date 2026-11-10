package ph.gov.santarosa.petcare.eligibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Behaviour the Administrator's Enable/Disable toggle adds on top of the JavaScript rules. */
class EligibilityEngineTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 17);
    private static final EligibilityEngine ENGINE = new EligibilityEngine();

    /** Buddy (seed P-1042): ELIGIBLE for Kapon with everything enabled. */
    private static PetFacts buddy() {
        return new PetFacts("Verified", "Aspin", "Brown, white chest", LocalDate.of(2024, 5, 10), new BigDecimal("9.5"),
                "Apparently Healthy", "Updated", LocalDate.of(2026, 2, 10), "Updated", LocalDate.of(2026, 7, 1),
                "Verified at Barangay", null, "Not Microchipped");
    }

    private static PetFacts underweightBuddy() {
        PetFacts b = buddy();
        return new PetFacts(b.registrationStatus(), b.breed(), b.color(), b.dob(), new BigDecimal("2.0"), b.healthStatus(),
                b.rabiesStatus(), b.lastRabies(), b.dewormingStatus(), b.lastDeworming(), b.proofOfResidency(),
                b.microchip(), b.microchipStatus());
    }

    @Test
    void allEnabledGivesEligible() {
        assertEquals(Classification.ELIGIBLE, ENGINE.assess(buddy(), ServiceNames.KAPON, TODAY).classification());
    }

    @Test
    void disablingAFailingCriterionRemovesItFromTheAssessment() {
        assertEquals(Classification.NOT_ELIGIBLE, ENGINE.assess(underweightBuddy(), ServiceNames.KAPON, TODAY).classification());
        List<String> withoutWeight = ENGINE.defaultKeys(ServiceNames.KAPON).stream().filter(k -> !k.equals("weight")).toList();
        AssessmentResult r = ENGINE.assess(underweightBuddy(), ServiceNames.KAPON, withoutWeight, TODAY);
        assertEquals(Classification.ELIGIBLE, r.classification());
        assertEquals(6, r.criteria().size());
    }

    @Test
    void criteriaFollowTheOrderGiven() {
        AssessmentResult r = ENGINE.assess(buddy(), ServiceNames.KAPON, List.of("weight", "registered"), TODAY);
        assertEquals("Weight Requirement Satisfied", r.criteria().get(0).label());
        assertEquals("Pet Registered and Verified", r.criteria().get(1).label());
    }

    @Test
    void nothingEnabledNeverAutoApproves() {
        AssessmentResult r = ENGINE.assess(buddy(), ServiceNames.KAPON, List.of(), TODAY);
        assertEquals(Classification.FOR_REVIEW, r.classification());
        assertEquals(2, r.decisionPath().size());   // START + FINAL
    }

    @Test
    void unknownCriterionKeyNeedsReviewInsteadOfPassing() {
        AssessmentResult r = ENGINE.assess(buddy(), ServiceNames.KAPON, List.of("registered", "someNewRule"), TODAY);
        assertEquals(Classification.FOR_REVIEW, r.classification());
    }

    @Test
    void unknownServiceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ENGINE.assess(buddy(), "Grooming", TODAY));
    }

    @Test
    void ageHelpersMatchTheUi() {
        assertEquals("Newborn", EligibilityEngine.ageString(LocalDate.of(2026, 8, 10), TODAY));
        assertEquals("7m", EligibilityEngine.ageString(LocalDate.of(2026, 1, 10), TODAY));
        assertEquals("1y", EligibilityEngine.ageString(LocalDate.of(2025, 8, 17), TODAY));
        assertEquals("2y 3m", EligibilityEngine.ageString(LocalDate.of(2024, 5, 10), TODAY));
    }
}
