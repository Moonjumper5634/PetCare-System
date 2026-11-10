package ph.gov.santarosa.petcare.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ph.gov.santarosa.petcare.domain.Pet;
import ph.gov.santarosa.petcare.service.PetWorkflowService.RegisterPet;
import ph.gov.santarosa.petcare.support.Kit;

class RegistrationFlowTest {

    Kit k;

    @BeforeEach
    void setUp() { k = new Kit(); }

    private int status(Runnable r) { return assertThrows(ApiException.class, r::run).getStatus(); }

    private RegisterPet with(UnaryOperator<RegisterPet> change) { return change.apply(k.newPetForm(null)); }

    @Test
    void residentRegistrationCreatesAPendingPetRegistrationNotificationAndAuditEntry() {
        PetWorkflowService.Registered r = k.petFlow.registerPet(k.maria, k.newPetForm(null));
        Pet p = k.pets.findById(r.petId()).orElseThrow();
        assertEquals("P-2000", p.getId());
        assertEquals("REG-2000", r.registrationId());
        assertEquals("Pending", p.getRegistrationStatus());
        assertNull(p.getRegisteredDate());
        assertEquals("Owner", p.getRegistrationSource());
        assertEquals("Dog", p.getSpecies());
        assertEquals("Pending", k.registrations.findById(r.registrationId()).orElseThrow().getStatus());
        assertEquals(TODAY(), k.registrations.findById(r.registrationId()).orElseThrow().getSubmittedDate());
        assertEquals(1, k.notifications.findByUserIdOrderByCreatedAtDesc("U-1001").size());
        assertEquals("U-1001", k.audit.findAll().get(0).getUserId());
    }

    @Test
    void nonresidentAccountCanBeUsedForAdoptionButNotCityPetRegistration() {
        k.maria.setBarangay("Outside Santa Rosa");
        assertEquals(400, status(() -> k.petFlow.registerPet(k.maria, k.newPetForm(null))));
        var form = new AdoptionWorkflowService.Apply("S-201", "Maria Santos", "09171234567", "maria@test.ph",
                "Calamba, Laguna", "Outside Santa Rosa", "Companionship", "Has cared for dogs", "Owned House", true);
        assertEquals("ADOPT-6000", k.adoptionFlow.apply(k.maria, form));
    }

    private static LocalDate TODAY() { return Kit.TODAY; }

    @Test
    void theRabiesStatusTheResidentSelectedIsKept() {
        // The original front end dropped this field, which made every new pet "unknown" for Kapon.
        Pet p = k.pets.findById(k.petFlow.registerPet(k.maria, k.newPetForm(null)).petId()).orElseThrow();
        assertEquals("Updated", p.getRabiesStatus());
        assertEquals(LocalDate.of(2026, 5, 1), p.getLastRabies());
        assertEquals("Updated", p.getDewormingStatus());
    }

    @Test
    void datesAreOnlyKeptWhenTheStatusIsUpdated() {
        Pet p = k.pets.findById(k.petFlow.registerPet(k.maria,
                with(f -> new RegisterPet(null, "Zed", "Dog", "Aspin", "Male", "2025-03-01", null, "Balibago", new BigDecimal("5"), "Apparently Healthy",
                        null, "Not Updated", "2026-05-01", "Unknown", "2026-06-01", "Not Microchipped", null, "Not Submitted"))).petId()).orElseThrow();
        assertNull(p.getLastRabies());
        assertNull(p.getLastDeworming());
    }

    @Test
    void aResidentCannotSelfDeclareVerifiedResidencyButStaffCan() {
        RegisterPet claim = with(f -> new RegisterPet(f.ownerId(), f.name(), f.species(), f.breed(), f.sex(), f.dob(), f.color(), f.barangay(), f.weight(),
                f.healthStatus(), f.healthNotes(), f.rabiesStatus(), f.lastRabies(), f.dewormingStatus(), f.lastDeworming(), f.microchipStatus(),
                f.microchipNumber(), "Verified at Barangay"));
        assertEquals("Submitted, Pending Verification", k.pets.findById(k.petFlow.registerPet(k.maria, claim).petId()).orElseThrow().getProofOfResidency());
        RegisterPet walkIn = new RegisterPet("U-1002", "Walker", "Dog", "Aspin", "Male", "2025-03-01", null, "Balibago", new BigDecimal("5"), "Apparently Healthy",
                null, "Unknown", null, "Unknown", null, "Not Microchipped", null, "Verified at Barangay");
        Pet p = k.pets.findById(k.petFlow.registerPet(k.rowena, walkIn).petId()).orElseThrow();
        assertEquals("Verified at Barangay", p.getProofOfResidency());
        assertEquals("Walk-in", p.getRegistrationSource());
    }

    @Test
    void invalidInputIsRefused() {
        assertEquals(400, status(() -> k.petFlow.registerPet(k.maria, with(f -> new RegisterPet(null, "", "Dog", "Aspin", "Male", "2025-03-01", null, "Balibago", new BigDecimal("5"), "Apparently Healthy", null, "Unknown", null, "Unknown", null, "Not Microchipped", null, "Not Submitted")))));
        assertEquals(400, status(() -> k.petFlow.registerPet(k.maria, with(f -> new RegisterPet(null, "A", "Cat", "Aspin", "Male", "2025-03-01", null, "Balibago", new BigDecimal("5"), "Apparently Healthy", null, "Unknown", null, "Unknown", null, "Not Microchipped", null, "Not Submitted")))));
        assertEquals(400, status(() -> k.petFlow.registerPet(k.maria, with(f -> new RegisterPet(null, "A", "Dog", "Aspin", "Robot", "2025-03-01", null, "Balibago", new BigDecimal("5"), "Apparently Healthy", null, "Unknown", null, "Unknown", null, "Not Microchipped", null, "Not Submitted")))));
        assertEquals(400, status(() -> k.petFlow.registerPet(k.maria, with(f -> new RegisterPet(null, "A", "Dog", "Aspin", "Male", "2999-01-01", null, "Balibago", new BigDecimal("5"), "Apparently Healthy", null, "Unknown", null, "Unknown", null, "Not Microchipped", null, "Not Submitted")))));
        assertEquals(400, status(() -> k.petFlow.registerPet(k.maria, with(f -> new RegisterPet(null, "A", "Dog", "Aspin", "Male", "not-a-date", null, "Balibago", new BigDecimal("5"), "Apparently Healthy", null, "Unknown", null, "Unknown", null, "Not Microchipped", null, "Not Submitted")))));
        assertEquals(400, status(() -> k.petFlow.registerPet(k.maria, with(f -> new RegisterPet(null, "A", "Dog", "Aspin", "Male", "2025-03-01", null, "Balibago", null, "Apparently Healthy", null, "Unknown", null, "Unknown", null, "Not Microchipped", null, "Not Submitted")))));
        assertEquals(400, status(() -> k.petFlow.registerPet(k.maria, with(f -> new RegisterPet(null, "A", "Dog", "Aspin", "Male", "2025-03-01", null, "Balibago", new BigDecimal("-3"), "Apparently Healthy", null, "Unknown", null, "Unknown", null, "Not Microchipped", null, "Not Submitted")))));
        assertEquals(400, status(() -> k.petFlow.registerPet(k.maria, with(f -> new RegisterPet(null, "A", "Dog", "Aspin", "Male", "2025-03-01", null, "Balibago", new BigDecimal("500"), "Apparently Healthy", null, "Unknown", null, "Unknown", null, "Not Microchipped", null, "Not Submitted")))));
        assertEquals(400, status(() -> k.petFlow.registerPet(k.maria, with(f -> new RegisterPet(null, "A", "Dog", "Aspin", "Male", "2025-03-01", null, "Balibago", new BigDecimal("5"), "Perfect", null, "Unknown", null, "Unknown", null, "Not Microchipped", null, "Not Submitted")))));
        assertEquals(400, status(() -> k.petFlow.registerPet(k.maria, with(f -> new RegisterPet(null, "x".repeat(200), "Dog", "Aspin", "Male", "2025-03-01", null, "Balibago", new BigDecimal("5"), "Apparently Healthy", null, "Unknown", null, "Unknown", null, "Not Microchipped", null, "Not Submitted")))));
        assertTrue(k.pets.findAll().stream().noneMatch(p -> p.getId().startsWith("P-20")), "nothing was saved");
    }

    @Test
    void duplicatePetAndMicrochipAreRefused() {
        k.petFlow.registerPet(k.maria, k.newPetForm(null));
        assertEquals(409, status(() -> k.petFlow.registerPet(k.maria, k.newPetForm(null))));          // same name + birth date
        k.petFlow.registerPet(k.juan, k.newPetForm(null));                                              // another owner may use the same name

        RegisterPet chipped = new RegisterPet(null, "Chip", "Dog", "Aspin", "Male", "2025-03-01", null, "Balibago", new BigDecimal("5"), "Apparently Healthy",
                null, "Unknown", null, "Unknown", null, "Microchipped", "985112003456789", "Not Submitted");
        k.petFlow.registerPet(k.maria, chipped);
        RegisterPet clash = new RegisterPet(null, "Other", "Dog", "Aspin", "Male", "2025-03-01", null, "Balibago", new BigDecimal("5"), "Apparently Healthy",
                null, "Unknown", null, "Unknown", null, "Microchipped", "985112003456789", "Not Submitted");
        assertEquals(409, status(() -> k.petFlow.registerPet(k.juan, clash)));
    }

    @Test
    void microchippedWithoutANumberIsAllowedAndStoresNoNumber() {
        RegisterPet f = new RegisterPet(null, "NoNum", "Dog", "Aspin", "Male", "2025-03-01", null, "Balibago", new BigDecimal("5"), "Apparently Healthy",
                null, "Unknown", null, "Unknown", null, "Microchipped", null, "Not Submitted");
        Pet p = k.pets.findById(k.petFlow.registerPet(k.maria, f).petId()).orElseThrow();
        assertEquals("Microchipped", p.getMicrochipStatus());
        assertNull(p.getMicrochip());
    }

    @Test
    void staffVerifyMakesThePetVerifiedAndStampsTheDate() {
        var r = k.petFlow.registerPet(k.maria, k.newPetForm(null));
        k.petFlow.verifyRegistration(k.rowena, r.registrationId(), false);
        Pet p = k.pets.findById(r.petId()).orElseThrow();
        assertEquals("Verified", p.getRegistrationStatus());
        assertEquals(Kit.TODAY, p.getRegisteredDate());
        assertEquals("U-2001", k.registrations.findById(r.registrationId()).orElseThrow().getReviewedBy());
        assertEquals(2, k.notifications.findByUserIdOrderByCreatedAtDesc("U-1001").size());
        assertEquals(409, status(() -> k.petFlow.verifyRegistration(k.rowena, r.registrationId(), false)));   // cannot review twice
        assertEquals(409, status(() -> k.petFlow.rejectRegistration(k.rowena, r.registrationId(), "too late")));
    }

    @Test
    void rejectNeedsAReasonAndKeepsItForTheResident() {
        var r = k.petFlow.registerPet(k.maria, k.newPetForm(null));
        assertEquals(400, status(() -> k.petFlow.rejectRegistration(k.rowena, r.registrationId(), "  ")));
        k.petFlow.rejectRegistration(k.rowena, r.registrationId(), "Photo does not match");
        assertEquals("Rejected", k.pets.findById(r.petId()).orElseThrow().getRegistrationStatus());
        assertEquals("Photo does not match", k.registrations.findById(r.registrationId()).orElseThrow().getRejectionReason());
        assertTrue(k.notifications.findByUserIdOrderByCreatedAtDesc("U-1001").get(0).getBody().contains("Photo does not match"));
    }

    @Test
    void residentCanCorrectAndResubmitOnlyTheirReturnedRegistration() {
        var made=k.petFlow.registerPet(k.maria,k.newPetForm(null));
        k.petFlow.rejectRegistration(k.rowena,made.registrationId(),"Correct the breed and weight");
        RegisterPet corrected=with(f->new RegisterPet(null,"New Dog","Dog","Labrador","Female",f.dob(),"Brown",f.barangay(),
                new BigDecimal("12.5"),f.healthStatus(),f.healthNotes(),f.rabiesStatus(),f.lastRabies(),f.dewormingStatus(),
                f.lastDeworming(),f.microchipStatus(),f.microchipNumber(),f.proofOfResidency()));
        assertEquals(404,status(()->k.petFlow.resubmitRegistration(k.juan,made.registrationId(),corrected)));
        k.petFlow.resubmitRegistration(k.maria,made.registrationId(),corrected);
        Pet p=k.pets.findById(made.petId()).orElseThrow();
        assertEquals("Labrador",p.getBreed()); assertEquals("Pending",p.getRegistrationStatus());
        assertEquals("Pending",k.registrations.findById(made.registrationId()).orElseThrow().getStatus());
        assertNull(k.registrations.findById(made.registrationId()).orElseThrow().getRejectionReason());
        assertEquals(409,status(()->k.petFlow.resubmitRegistration(k.maria,made.registrationId(),corrected)));
    }

    @Test
    void unknownRegistrationIsNotFound() {
        assertEquals(404, status(() -> k.petFlow.verifyRegistration(k.rowena, "REG-0", false)));
        assertEquals(404, status(() -> k.petFlow.verifyRegistration(k.rowena, null, false)));
    }
}
