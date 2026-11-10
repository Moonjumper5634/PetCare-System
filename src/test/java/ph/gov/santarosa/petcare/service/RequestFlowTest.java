package ph.gov.santarosa.petcare.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ph.gov.santarosa.petcare.domain.EligibilityAssessment;
import ph.gov.santarosa.petcare.domain.Pet;
import ph.gov.santarosa.petcare.domain.ServiceRequest;
import ph.gov.santarosa.petcare.service.RequestWorkflowService.CreateRequest;
import ph.gov.santarosa.petcare.service.RequestWorkflowService.Decision;
import ph.gov.santarosa.petcare.service.RequestWorkflowService.RecordService;
import ph.gov.santarosa.petcare.support.Kit;

class RequestFlowTest {

    static final String KAPON = "Kapon / Spay-Neuter";
    Kit k;

    @BeforeEach
    void setUp() { k = new Kit(); }

    private int status(Runnable r) { return assertThrows(ApiException.class, r::run).getStatus(); }
    /** Submits as the pet's real owner (P-1050 is Juan's dog; a resident cannot use someone else's pet). */
    private String request(String petId, String service) {
        return k.requestFlow.createRequest(petId.equals("P-1050") ? k.juan : k.maria, new CreateRequest(petId, service, "please"));
    }
    private ServiceRequest get(String id) { return k.requests.findById(id).orElseThrow(); }
    private RecordService rec(String chip) { return new RecordService(null, null, "ok", "Rabisin", "AR-1", "2027-08-17", chip, null, null, null, null, null, null, null); }

    /* ---- submit ---- */

    @Test
    void submitCreatesASubmittedRequestOwnedByThePetOwner() {
        String id = request("P-1042", KAPON);
        ServiceRequest r = get(id);
        assertEquals("REQ-4000", id);
        assertEquals("Submitted", r.getStatus());
        assertEquals("Pending", r.getStaffDecision());
        assertEquals("Owner", r.getSource());
        assertEquals("U-1001", r.getOwnerId());
        assertEquals(KAPON, r.getServiceName());
    }

    @Test
    void walkInRequestUsesThePetsOwner() {
        String id = k.requestFlow.createRequest(k.rowena, new CreateRequest("P-1050", "Microchipping", null));
        assertEquals("Walk-in", get(id).getSource());
        assertEquals("U-1002", get(id).getOwnerId());
    }

    @Test
    void onlyVerifiedPetsKnownServicesAndOneOpenRequestPerService() {
        assertEquals(400, status(() -> request("P-1051", "Microchipping")));           // pending registration
        assertEquals(400, status(() -> request("P-1042", "Grooming")));                  // not a service
        assertEquals(400, status(() -> request("P-1042", null)));
        request("P-1042", "Microchipping");
        assertEquals(409, status(() -> request("P-1042", "Microchipping")));             // duplicate while open
        request("P-1042", KAPON);                                                        // another service is fine
    }

    /* ---- eligibility assessment ---- */

    @Test
    void assessStoresTheDecisionTreeResultAndMovesTheRequestToForReview() {
        String id = request("P-1042", KAPON);
        k.requestFlow.assess(k.rowena, id);
        EligibilityAssessment a = k.assessments.findByRequestId(id).orElseThrow();
        assertEquals("ELIGIBLE", a.getClassification());
        assertEquals("U-2001", a.getAssessedBy());
        assertEquals(7, k.assessmentCriteria.findByAssessmentIdOrderBySeq(a.getId()).size());
        assertEquals("For Review", get(id).getStatus());
        assertEquals("Pending", get(id).getStaffDecision());
    }

    @Test
    void assessingAgainReplacesTheRowsInsteadOfDuplicatingThem() {
        String id = request("P-1042", KAPON);
        k.requestFlow.assess(k.rowena, id);
        k.pets.findById("P-1042").orElseThrow().setWeightKg(new java.math.BigDecimal("2.0"));      // pet changed since
        k.requestFlow.assess(k.rowena, id);
        EligibilityAssessment a = k.assessments.findByRequestId(id).orElseThrow();
        assertEquals(1, k.assessments.findAll().size());
        assertEquals("NOT ELIGIBLE", a.getClassification());
        assertEquals(7, k.assessmentCriteria.findByAssessmentIdOrderBySeq(a.getId()).size());
    }

    @Test
    void theAdministratorsToggleChangesTheAssessment() {
        k.pets.findById("P-1042").orElseThrow().setWeightKg(new java.math.BigDecimal("2.0"));
        String id = request("P-1042", KAPON);
        k.requestFlow.assess(k.rowena, id);
        assertEquals("NOT ELIGIBLE", k.assessments.findByRequestId(id).orElseThrow().getClassification());
        k.adminFlow.updateCriterion(k.jose, KAPON, "weight", new AdminService.CriterionUpdate(false, null));
        k.requestFlow.assess(k.rowena, id);
        EligibilityAssessment a = k.assessments.findByRequestId(id).orElseThrow();
        assertEquals("ELIGIBLE", a.getClassification());
        assertEquals(6, k.assessmentCriteria.findByAssessmentIdOrderBySeq(a.getId()).size());
    }

    @Test
    void previewSavesNothing() {
        assertEquals("ELIGIBLE", k.requestFlow.preview(k.maria, "P-1042", KAPON).classification().label());
        assertTrue(k.assessments.findAll().isEmpty());
    }

    /* ---- staff decision ---- */

    @Test
    void approveNeedsAnAssessmentFirst() {
        String id = request("P-1042", KAPON);
        assertEquals(400, status(() -> k.requestFlow.decide(k.rowena, id, new Decision("Approved", null))));
        k.requestFlow.assess(k.rowena, id);
        k.requestFlow.decide(k.rowena, id, new Decision("Approved", null));
        ServiceRequest r = get(id);
        assertEquals("Approved", r.getStatus());
        assertEquals("Approved", r.getStaffDecision());
        assertEquals(Kit.TODAY, r.getDecisionDate());
        assertEquals("U-2001", r.getDecidedBy());
    }

    @Test
    void aDecidedRequestCannotBeDecidedOrAssessedAgain() {
        String id = request("P-1042", KAPON);
        k.requestFlow.assess(k.rowena, id);
        k.requestFlow.decide(k.rowena, id, new Decision("Approved", null));
        assertEquals(409, status(() -> k.requestFlow.decide(k.rowena, id, new Decision("Rejected", "changed my mind"))));
        assertEquals(409, status(() -> k.requestFlow.assess(k.rowena, id)));
    }

    @Test
    void rejectNeedsAReasonAndTheResidentSeesIt() {
        String id = request("P-1042", KAPON);
        assertEquals(400, status(() -> k.requestFlow.decide(k.rowena, id, new Decision("Rejected", " "))));
        k.requestFlow.decide(k.rowena, id, new Decision("Rejected", "Too young"));
        assertEquals("Rejected", get(id).getStatus());
        assertEquals("Too young", get(id).getRejectionReason());
        assertTrue(k.notifications.findByUserIdOrderByCreatedAtDesc("U-1001").get(0).getBody().contains("Too young"));
    }

    @Test
    void verificationRequestKeepsTheRequestOpen() {
        String id = request("P-1042", KAPON);
        k.requestFlow.decide(k.rowena, id, new Decision("Verification Requested", null));
        assertEquals("For Review", get(id).getStatus());
        assertEquals("Verification Requested", get(id).getStaffDecision());
        k.requestFlow.assess(k.rowena, id);                                              // can still be assessed and decided
        k.requestFlow.decide(k.rowena, id, new Decision("Approved", null));
        assertEquals("Approved", get(id).getStatus());
    }

    @Test
    void unknownDecisionIsRefused() {
        String id = request("P-1042", KAPON);
        assertEquals(400, status(() -> k.requestFlow.decide(k.rowena, id, new Decision("Completed", null))));
        assertEquals(400, status(() -> k.requestFlow.decide(k.rowena, id, new Decision(null, null))));
    }

    /* ---- service record ---- */

    private String approved(String petId, String service) {
        String id = request(petId, service);
        k.requestFlow.assess(k.rowena, id);
        k.requestFlow.decide(k.rowena, id, new Decision("Approved", null));
        return id;
    }

    @Test
    void recordingAnApprovedRabiesVaccinationUpdatesThePetAndCompletesTheRequest() {
        k.pets.findById("P-1042").orElseThrow().setLastRabies(LocalDate.of(2024, 1, 1));
        String id = approved("P-1042", "Anti-Rabies Vaccination");
        k.requestFlow.recordService(k.rowena, id, rec(null));
        Pet p = k.pets.findById("P-1042").orElseThrow();
        assertEquals(Kit.TODAY, p.getLastRabies());
        assertEquals("Updated", p.getRabiesStatus());
        assertEquals("Completed", get(id).getStatus());
        var record = k.records.findByRequestId(id).orElseThrow();
        assertEquals("Rabisin", record.getVaccineType());
        assertEquals("Dr. Rowena Tan", record.getStaffName());                           // defaults to the staff member
        assertEquals(LocalDate.of(2027, 8, 17), record.getNextDue());
    }

    @Test
    void serviceRecordKeepsPresenterProviderSourceAndLocationSeparate() {
        String id = approved("P-1042", "Anti-Rabies Vaccination");
        k.requestFlow.recordService(k.rowena, id, new RecordService("2026-08-16", "Juan Vaccinator", null,
                "Rabisin", "LOT-9", "2027-08-16", null, null, null,
                "Ana Santos", "Trained Vaccinator", "Barangay activity", "SLIP-77", "Balibago Covered Court"));
        var record = k.records.findByRequestId(id).orElseThrow();
        assertEquals("Ana Santos", record.getPresenterName());
        assertEquals("Trained Vaccinator", record.getProviderRole());
        assertEquals("Barangay activity", record.getSourceType());
        assertEquals("SLIP-77", record.getSourceReference());
        assertEquals("Balibago Covered Court", record.getServiceLocation());
    }

    @Test
    void recordingMicrochippingStoresTheNumberAndRefusesDuplicates() {
        String id = approved("P-1042", "Microchipping");
        assertEquals(400, status(() -> k.requestFlow.recordService(k.rowena, id, rec(null))));        // number required
        assertEquals(400, status(() -> k.requestFlow.recordService(k.rowena, id, rec("bad number!"))));
        k.requestFlow.recordService(k.rowena, id, rec("900182820012345"));
        Pet p = k.pets.findById("P-1042").orElseThrow();
        assertEquals("900182820012345", p.getMicrochip());
        assertEquals("Microchipped", p.getMicrochipStatus());

        String other = approved("P-1050", "Microchipping");
        assertEquals(409, status(() -> k.requestFlow.recordService(k.rowena, other, rec("900182820012345"))));   // same chip, other dog
    }

    @Test
    void recordingKaponMarksTheProcedure() {
        String id = approved("P-1042", KAPON);
        k.requestFlow.recordService(k.rowena, id, new RecordService("2026-08-15", null, null, null, null, null, null, "2026-08-14", "Recovering well", null, null, null, null, null));
        Pet p = k.pets.findById("P-1042").orElseThrow();
        assertEquals("Completed", p.getKaponStatus());
        assertEquals(LocalDate.of(2026, 8, 14), p.getKaponDate());
        assertEquals("Recovering well", k.records.findByRequestId(id).orElseThrow().getPostRemarks());
    }

    @Test
    void onlyApprovedRequestsCanBeRecordedAndOnlyOnce() {
        String id = request("P-1042", "Microchipping");
        assertEquals(409, status(() -> k.requestFlow.recordService(k.rowena, id, rec("123456"))));    // not approved yet
        k.requestFlow.assess(k.rowena, id);
        k.requestFlow.decide(k.rowena, id, new Decision("Approved", null));
        k.requestFlow.recordService(k.rowena, id, rec("123456"));
        assertEquals(409, status(() -> k.requestFlow.recordService(k.rowena, id, rec("654321"))));    // already completed
        assertEquals(400, status(() -> k.requestFlow.recordService(k.rowena, approved("P-1050", "Microchipping"),
                new RecordService("2999-01-01", null, null, null, null, null, "777", null, null, null, null, null, null, null))));    // service date in the future
    }

    /* ---- QR quick visit ---- */

    @Test
    void qrQuickVisitCreatesAPendingRequestThatRequiresAssessment() {
        String id = k.requestFlow.quickVisit(k.rowena, "P-1042", "Anti-Rabies Vaccination");
        ServiceRequest r = get(id);
        assertEquals("QR Scan", r.getSource());
        assertEquals("Submitted", r.getStatus());
        assertEquals("Pending", r.getStaffDecision());
        assertTrue(k.assessments.findByRequestId(id).isEmpty());
        assertEquals(409, status(() -> k.requestFlow.recordService(k.rowena, id, rec(null))));
        k.requestFlow.assess(k.rowena, id);
        k.requestFlow.decide(k.rowena, id, new Decision("Approved", null));
        k.requestFlow.recordService(k.rowena, id, rec(null));
        assertEquals("Completed", get(id).getStatus());
        assertEquals(400, status(() -> k.requestFlow.quickVisit(k.rowena, "P-1051", "Microchipping")));   // pet not verified
        assertEquals(404, status(() -> k.requestFlow.quickVisit(k.rowena, "P-0", "Microchipping")));
        assertEquals("U-2001", r.getDecidedBy());
    }
}
