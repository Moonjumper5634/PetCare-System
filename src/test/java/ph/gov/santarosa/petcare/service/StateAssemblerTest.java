package ph.gov.santarosa.petcare.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ph.gov.santarosa.petcare.support.Kit;

/** What each role is allowed to receive, and that nothing secret is ever included. */
class StateAssemblerTest {

    Kit k;

    @BeforeEach
    void setUp() {
        k = new Kit();
        String id = k.requestFlow.createRequest(k.maria, new RequestWorkflowService.CreateRequest("P-1042", "Kapon / Spay-Neuter", null));
        k.requestFlow.assess(k.rowena, id);
        k.requestFlow.createRequest(k.juan, new RequestWorkflowService.CreateRequest("P-1050", "Microchipping", null));
        k.adoptionFlow.apply(k.juan, new AdoptionWorkflowService.Apply("S-201", "Juan", "09171234567", "j@test.ph", "x", "Balibago", "r", null, "Owned House", true));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> state(Map<String, Object> boot) { return (Map<String, Object>) boot.get("state"); }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Map<String, Object> boot, String key) { return (List<Map<String, Object>>) state(boot).get(key); }

    private Set<Object> ids(List<Map<String, Object>> rows) { return rows.stream().map(r -> r.get("id")).collect(Collectors.toSet()); }

    @Test
    void aResidentReceivesOnlyTheirOwnRecords() {
        Map<String, Object> boot = k.assembler.bootstrap(k.maria);
        assertEquals(Set.of("U-1001"), ids(list(boot, "users")));
        assertEquals(Set.of("P-1042", "P-1051"), ids(list(boot, "pets")));
        assertEquals(1, list(boot, "requests").size());
        assertTrue(list(boot, "adoptions").isEmpty(), "Juan's adoption application is not Maria's");
        assertTrue(list(boot, "audit").isEmpty());
        assertTrue(((Map<?, ?>) state(boot).get("criteria")).isEmpty());
        assertTrue(list(boot, "notifications").stream().allMatch(n -> n.get("userId").equals("U-1001")));
        assertEquals(1, list(boot, "shelterPets").size());                   // the shelter dogs are public to residents
    }

    @Test
    void anotherResidentSeesNothingOfMarias() {
        Map<String, Object> boot = k.assembler.bootstrap(k.juan);
        assertEquals(Set.of("P-1050"), ids(list(boot, "pets")));
        assertFalse(boot.toString().contains("Buddy"));
        assertFalse(boot.toString().contains("maria@test.ph"));
        assertEquals(1, list(boot, "adoptions").size());
    }

    @Test
    void staffSeeAllResidentsPetsAndRequestsButNotOtherStaffOrAdmins() {
        Map<String, Object> boot = k.assembler.bootstrap(k.rowena);
        assertEquals(Set.of("U-1001", "U-1002", "U-2001"), ids(list(boot, "users")));
        assertEquals(3, list(boot, "pets").size());
        assertEquals(2, list(boot, "requests").size());
        assertTrue(list(boot, "audit").isEmpty(), "action history is restricted to administrators");
        assertTrue(((Map<?, ?>) state(boot).get("criteria")).isEmpty(), "criteria are an administrator screen");
    }

    @Test
    void administratorsSeeEveryoneAndTheCriteria() {
        Map<String, Object> boot = k.assembler.bootstrap(k.jose);
        assertEquals(Set.of("U-1001", "U-1002", "U-2001", "U-3001"), ids(list(boot, "users")));
        assertFalse(list(boot, "audit").isEmpty(), "administrators receive the named action history");
        Map<?, ?> criteria = (Map<?, ?>) state(boot).get("criteria");
        assertEquals(List.of("Anti-Rabies Vaccination", "Microchipping", "Kapon / Spay-Neuter"), List.copyOf(criteria.keySet()));
        assertEquals(7, ((List<?>) criteria.get("Kapon / Spay-Neuter")).size());
    }

    @Test
    void secretsAreNeverIncluded() {
        k.maria.setVerificationCode("987654");
        for (var who : List.of(k.maria, k.rowena, k.jose)) {
            String json = k.assembler.bootstrap(who).toString();
            assertFalse(json.contains("enc:"), "password hash leaked to " + who.getRole());
            assertFalse(json.contains("passwordHash"));
            assertFalse(json.contains("987654"), "verification code leaked");
            assertFalse(json.contains("verificationCode"));
            assertFalse(json.contains("failedLogin"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void theShapeMatchesWhatTheFrontEndReads() {
        Map<String, Object> boot = k.assembler.bootstrap(k.rowena);
        assertEquals("2026-08-17", boot.get("today"));
        Map<String, Object> user = (Map<String, Object>) boot.get("user");
        assertEquals("Dr. Rowena Tan", user.get("name"));
        assertTrue(user.containsKey("tag") && user.containsKey("mustChangePassword"));

        Map<String, Object> pet = list(boot, "pets").stream().filter(p -> p.get("id").equals("P-1042")).findFirst().orElseThrow();
        for (String key : List.of("id", "ownerId", "name", "species", "breed", "sex", "dob", "color", "barangay", "registrationStatus", "registeredDate",
                "registrationSource", "weight", "healthStatus", "healthNotes", "rabiesStatus", "lastRabies", "dewormingStatus", "lastDeworming",
                "proofOfResidency", "microchip", "microchipStatus", "kaponStatus", "kaponDate", "photo"))
            assertTrue(pet.containsKey(key), "pet." + key);
        assertEquals(9.5, pet.get("weight"));
        assertEquals("2024-05-10", pet.get("dob"));

        Map<String, Object> req = list(boot, "requests").stream().filter(r -> r.get("service").equals("Kapon / Spay-Neuter")).findFirst().orElseThrow();
        Map<String, Object> eligibility = (Map<String, Object>) req.get("eligibility");
        assertEquals("ELIGIBLE", eligibility.get("classification"));
        assertEquals(true, eligibility.get("eligible"));
        List<Map<String, Object>> path = (List<Map<String, Object>>) eligibility.get("decisionPath");
        assertEquals("start", path.get(0).get("type"));
        assertEquals("final", path.get(path.size() - 1).get("type"));
        assertEquals(9, path.size());                                                          // START + 7 criteria + FINAL
        assertNull(list(boot, "requests").stream().filter(r -> r.get("service").equals("Microchipping")).findFirst().orElseThrow().get("eligibility"));
    }

    @Test
    void recordsAreInIdOrderAndNewestNotificationsComeFirst() {
        List<Map<String, Object>> reqs = list(k.assembler.bootstrap(k.rowena), "requests");
        assertEquals(List.of("REQ-4000", "REQ-4001"), reqs.stream().map(r -> r.get("id")).toList());
        List<Map<String, Object>> notes = list(k.assembler.bootstrap(k.maria), "notifications");
        assertEquals(2, notes.size());
        assertNotNull(notes.get(0).get("date"));
        assertTrue(notes.get(0).get("title").toString().contains("assessment"), "the assessment notice is newer than the submission");
    }

    @Test
    void numericIdsSortNumericallyNotAlphabetically() {
        assertTrue(StateAssembler.numberPart("REQ-999") < StateAssembler.numberPart("REQ-1000"));
        assertEquals(Long.MAX_VALUE, StateAssembler.numberPart("garbage"));
    }
}
