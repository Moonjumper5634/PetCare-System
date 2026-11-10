package ph.gov.santarosa.petcare.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.service.AdoptionWorkflowService.Apply;
import ph.gov.santarosa.petcare.service.AdoptionWorkflowService.Interview;
import ph.gov.santarosa.petcare.support.Kit;

class AdoptionAndAdminFlowTest {

    Kit k;

    @BeforeEach
    void setUp() { k = new Kit(); }

    private int status(Runnable r) { return assertThrows(ApiException.class, r::run).getStatus(); }
    private Apply form(String dog) { return new Apply(dog, "Maria Santos", "09171234567", "m@test.ph", "12 Rizal St", "Balibago", "I love dogs", "Had dogs", "Owned House", true); }
    private String applied() { return k.adoptionFlow.apply(k.maria, form("S-201")); }
    private String interviewed() { String id = applied(); k.adoptionFlow.recordInterview(k.rowena, id, new Interview("Good home", "Recommended", List.of("Home visit"))); return id; }

    /* ---- adoption ---- */

    @Test
    void applyingReservesTheDog() {
        String id = applied();
        assertEquals("Interview Required", k.adoptions.findById(id).orElseThrow().getStatus());
        assertEquals("Application Pending", k.shelterPets.findById("S-201").orElseThrow().getStatus());
        assertEquals("U-1001", k.adoptions.findById(id).orElseThrow().getApplicantOwnerId());
        assertEquals(409, status(() -> k.adoptionFlow.apply(k.juan, form("S-201"))));      // someone already applied
        assertEquals(404, status(() -> k.adoptionFlow.apply(k.maria, form("S-999"))));
    }

    @Test
    void adoptionFormIsValidated() {
        assertEquals(400, status(() -> k.adoptionFlow.apply(k.maria, new Apply("S-201", "", "0917", "m@test.ph", "x", "Balibago", "r", null, "Owned House", true))));
        assertEquals(400, status(() -> k.adoptionFlow.apply(k.maria, new Apply("S-201", "M", "0917123456", "bad", "x", "Balibago", "r", null, "Owned House", true))));
        assertEquals(400, status(() -> k.adoptionFlow.apply(k.maria, new Apply("S-201", "M", "0917123456", "m@test.ph", "x", "Balibago", "r", null, "Castle", true))));
        assertEquals(400, status(() -> k.adoptionFlow.apply(k.maria, new Apply("S-201", "M", "0917123456", "m@test.ph", "x", "Balibago", "r", null, "Owned House", false))));
        assertEquals("Available", k.shelterPets.findById("S-201").orElseThrow().getStatus());
    }

    @Test
    void interviewThenApproveMarksTheDogAdopted() {
        String id = interviewed();
        assertEquals("Interview Completed", k.adoptions.findById(id).orElseThrow().getStatus());
        assertEquals("Recommended", k.interviews.findByAdoptionId(id).orElseThrow().getRecommendation());
        assertEquals("[\"Home visit\"]", k.interviews.findByAdoptionId(id).orElseThrow().getChecklist());
        k.adoptionFlow.approve(k.rowena, id);
        assertEquals("Approved", k.adoptions.findById(id).orElseThrow().getStatus());
        assertEquals("Adopted", k.shelterPets.findById("S-201").orElseThrow().getStatus());
        assertEquals(409, status(() -> k.adoptionFlow.reject(k.rowena, id, "late")));
    }

    @Test
    void cannotApproveBeforeTheInterviewOrInterviewTwice() {
        String id = applied();
        assertEquals(409, status(() -> k.adoptionFlow.approve(k.rowena, id)));
        k.adoptionFlow.recordInterview(k.rowena, id, new Interview(null, null, null));
        assertEquals("Further Review", k.interviews.findByAdoptionId(id).orElseThrow().getRecommendation());   // the UI default
        assertEquals(409, status(() -> k.adoptionFlow.recordInterview(k.rowena, id, new Interview("again", "Recommended", null))));
        assertEquals(400, status(() -> k.adoptionFlow.recordInterview(k.rowena, applied2(), new Interview(null, "Maybe", null))));
    }

    private String applied2() { k.shelterPets.findById("S-201").orElseThrow().setStatus("Available"); return k.adoptionFlow.apply(k.juan, form("S-201")); }

    @Test
    void rejectionNeedsAReasonAndReleasesTheDog() {
        String id = applied();
        assertEquals(400, status(() -> k.adoptionFlow.reject(k.rowena, id, "")));
        k.adoptionFlow.reject(k.rowena, id, "Home not suitable");
        assertEquals("Rejected", k.adoptions.findById(id).orElseThrow().getStatus());
        assertEquals("Available", k.shelterPets.findById("S-201").orElseThrow().getStatus());
        assertTrue(k.notifications.findByUserIdOrderByCreatedAtDesc("U-1001").get(0).getBody().contains("Home not suitable"));
    }

    /* ---- walk-in resident account ---- */

    @Test
    void walkInResidentGetsARandomTemporaryPasswordThatMustBeChanged() {
        AdminService.ResidentCreated a = k.adminFlow.createResident(k.rowena, new AdminService.CreateResident("Pedro Dela Rosa", "09171234567", null, "5 Mabini", "Dita"));
        AdminService.ResidentCreated b = k.adminFlow.createResident(k.rowena, new AdminService.CreateResident("Ana Cruz", null, "ana.cruz@test.ph", null, "Pooc"));
        assertNotEquals(a.temporaryPassword(), b.temporaryPassword());
        assertNotEquals("changeme123", a.temporaryPassword());
        assertEquals(12, a.temporaryPassword().length());
        AppUser u = k.users.findById(a.userId()).orElseThrow();
        assertTrue(u.getMustChangePassword());
        assertEquals("owner", u.getRole());
        assertEquals("Pedro", u.getFirstName());
        assertEquals("Dela Rosa", u.getLastName());
        assertTrue(u.getEmail().endsWith("@walk-in.invalid"));                        // placeholder when no email was given
        assertEquals("ana.cruz@test.ph", b.email());
        assertEquals(a.userId(), k.authFlow.login(a.email(), a.temporaryPassword()).getId());   // the temporary password works
        assertEquals(409, assertThrows(ApiException.class, () -> k.adminFlow.createResident(k.rowena, new AdminService.CreateResident("Ana Two", null, "ANA.CRUZ@test.ph", null, "Pooc"))).getStatus());
        assertEquals(400, assertThrows(ApiException.class, () -> k.adminFlow.createResident(k.rowena, new AdminService.CreateResident(" ", null, null, null, "Pooc"))).getStatus());
    }

    @Test
    void temporaryPasswordBlocksActionsUntilChangedViaCurrentUserRule() {
        // CurrentUser.requireReady() is what enforces this in the web layer; here we check the flag it reads.
        AdminService.ResidentCreated a = k.adminFlow.createResident(k.rowena, new AdminService.CreateResident("Pedro Dela Rosa", null, null, null, "Dita"));
        AppUser u = k.authFlow.login(a.email(), a.temporaryPassword());
        assertTrue(u.getMustChangePassword());
        k.authFlow.changePassword(u, a.temporaryPassword(), "MyOwnPass123");
        assertFalse(k.users.findById(u.getId()).orElseThrow().getMustChangePassword());
    }

    @Test
    void onlyAnAdministratorCanProvisionPrivilegedPersonnel() {
        var made = k.adminFlow.createPersonnel(k.jose,
                new AdminService.CreatePersonnel("Dr Ana Cruz", "ana.cvo@test.ph", "09171234567", "staff", "Veterinarian"));
        AppUser u = k.users.findById(made.userId()).orElseThrow();
        assertEquals("staff", u.getRole());
        assertEquals("Veterinarian", u.getJobTitle());
        assertTrue(u.getMustChangePassword());
        assertEquals(made.userId(), k.authFlow.login(made.email(), made.temporaryPassword()).getId());
        assertEquals(403, status(() -> k.adminFlow.createPersonnel(k.rowena,
                new AdminService.CreatePersonnel("Bad Actor", "bad@test.ph", null, "admin", "Administrator"))));
        assertEquals(400, status(() -> k.adminFlow.createPersonnel(k.jose,
                new AdminService.CreatePersonnel("Wrong Role", "wrong@test.ph", null, "owner", "Resident"))));
    }

    /* ---- user status ---- */

    @Test
    void administratorCanDeactivateAndReactivateAResident() {
        k.adminFlow.setUserStatus(k.jose, "U-1001", "Inactive");
        assertEquals("Inactive", k.users.findById("U-1001").orElseThrow().getStatus());
        assertEquals(403, assertThrows(ApiException.class, () -> k.authFlow.login("maria@test.ph", "Resident#2026")).getStatus());
        k.adminFlow.setUserStatus(k.jose, "U-1001", "Active");
        assertEquals("U-1001", k.authFlow.login("maria@test.ph", "Resident#2026").getId());
    }

    @Test
    void administratorCannotLockThemselvesOrTheSystemOut() {
        assertEquals(400, status(() -> k.adminFlow.setUserStatus(k.jose, "U-3001", "Inactive")));      // own account
        AppUser second = k.user("U-3002", "admin", "Second Admin", "a2@test.ph", "Admin#2026");
        k.adminFlow.setUserStatus(k.jose, "U-3002", "Inactive");                                          // fine: two active admins before
        assertEquals(409, status(() -> k.adminFlow.setUserStatus(second, "U-3001", "Inactive")));      // jose is now the last active admin
        assertEquals("Active", k.users.findById("U-3001").orElseThrow().getStatus());
        assertEquals(400, status(() -> k.adminFlow.setUserStatus(k.jose, "U-1001", "Banned")));
        assertEquals(404, status(() -> k.adminFlow.setUserStatus(k.jose, "U-0", "Inactive")));
    }

    @Test
    void pendingAccountsCannotBeToggled() {
        k.maria.setStatus("Pending Verification");
        assertEquals(409, status(() -> k.adminFlow.setUserStatus(k.jose, "U-1001", "Active")));
    }

    /* ---- criteria ---- */

    @Test
    void criteriaToggleAndDescriptionAreAuditedAndValidated() {
        k.adminFlow.updateCriterion(k.jose, "Microchipping", "info", new AdminService.CriterionUpdate(false, "New text"));
        var c = k.criteria.findByServiceNameAndCriterionKey("Microchipping", "info").orElseThrow();
        assertFalse(c.getEnabled());
        assertEquals("New text", c.getDescription());
        assertEquals(2, k.audit.findAll().size());
        assertEquals(404, status(() -> k.adminFlow.updateCriterion(k.jose, "Microchipping", "nope", new AdminService.CriterionUpdate(true, null))));
        assertEquals(400, status(() -> k.adminFlow.updateCriterion(k.jose, "Microchipping", "info", new AdminService.CriterionUpdate(null, null))));
        assertEquals(400, status(() -> k.adminFlow.updateCriterion(k.jose, "Microchipping", "info", new AdminService.CriterionUpdate(null, "  "))));
    }
}
