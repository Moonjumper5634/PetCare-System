package ph.gov.santarosa.petcare.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ph.gov.santarosa.petcare.support.Kit;

/** Who may do what. A resident must never be able to reach staff actions or another resident's records. */
class PermissionsTest {

    Kit k;
    String reqOfMaria;

    @BeforeEach
    void setUp() {
        k = new Kit();
        reqOfMaria = k.requestFlow.createRequest(k.maria, new RequestWorkflowService.CreateRequest("P-1042", "Microchipping", null));
    }

    private static void denied(int status, Executable action) {
        ApiException e = assertThrows(ApiException.class, action);
        assertEquals(status, e.getStatus(), e.getMessage());
    }

    @Test
    void residentCannotUseAnyStaffAction() {
        denied(403, () -> k.requestFlow.assess(k.maria, reqOfMaria));
        denied(403, () -> k.requestFlow.decide(k.maria, reqOfMaria, new RequestWorkflowService.Decision("Approved", null)));
        denied(403, () -> k.requestFlow.recordService(k.maria, reqOfMaria, new RequestWorkflowService.RecordService(null, null, null, null, null, null, "123", null, null, null, null, null, null, null)));
        denied(403, () -> k.requestFlow.quickVisit(k.maria, "P-1042", "Microchipping"));
        denied(403, () -> k.petFlow.verifyRegistration(k.maria, "REG-1", false));
        denied(403, () -> k.petFlow.rejectRegistration(k.maria, "REG-1", "no"));
        denied(403, () -> k.adoptionFlow.approve(k.maria, "ADOPT-1"));
        denied(403, () -> k.adoptionFlow.reject(k.maria, "ADOPT-1", "no"));
        denied(403, () -> k.adoptionFlow.recordInterview(k.maria, "ADOPT-1", new AdoptionWorkflowService.Interview(null, null, null)));
        denied(403, () -> k.adminFlow.createResident(k.maria, new AdminService.CreateResident("X Y", null, null, null, "Balibago")));
    }

    @Test
    void residentAndStaffCannotUseAdministratorActions() {
        for (var who : List.of(k.maria, k.rowena)) {
            denied(403, () -> k.adminFlow.setUserStatus(who, "U-1002", "Inactive"));
            denied(403, () -> k.adminFlow.updateCriterion(who, "Kapon / Spay-Neuter", "weight", new AdminService.CriterionUpdate(false, null)));
        }
    }

    @Test
    void adminHasNoPetOrRequestActions() {
        denied(403, () -> k.petFlow.registerPet(k.jose, k.newPetForm("U-1001")));
        denied(403, () -> k.requestFlow.createRequest(k.jose, new RequestWorkflowService.CreateRequest("P-1042", "Microchipping", null)));
        denied(403, () -> k.requestFlow.assess(k.jose, reqOfMaria));
    }

    @Test
    void staffCannotApplyForAdoption() {
        denied(403, () -> k.adoptionFlow.apply(k.rowena, adoptionForm()));
        denied(403, () -> k.adoptionFlow.apply(k.jose, adoptionForm()));
    }

    @Test
    void residentCannotSeeOrUseSomeoneElsesPet() {
        // Juan's dog P-1050: to Maria it looks exactly like a pet that does not exist.
        denied(404, () -> k.requestFlow.createRequest(k.maria, new RequestWorkflowService.CreateRequest("P-1050", "Microchipping", null)));
        denied(404, () -> k.requestFlow.preview(k.maria, "P-1050", "Kapon / Spay-Neuter"));
        denied(404, () -> k.requestFlow.createRequest(k.maria, new RequestWorkflowService.CreateRequest("P-9999", "Microchipping", null)));
    }

    @Test
    void residentCannotRegisterAPetForAnotherResident() {
        // The ownerId in the body is ignored for residents: the pet always belongs to the signed-in user.
        var reg = k.petFlow.registerPet(k.maria, k.newPetForm("U-1002"));
        assertEquals("U-1001", k.pets.findById(reg.petId()).orElseThrow().getOwnerId());
    }

    @Test
    void staffMustNameAnActiveResidentForWalkIn() {
        denied(400, () -> k.petFlow.registerPet(k.rowena, k.newPetForm(null)));
        denied(400, () -> k.petFlow.registerPet(k.rowena, k.newPetForm("U-3001")));      // an administrator is not a resident
        denied(400, () -> k.petFlow.registerPet(k.rowena, k.newPetForm("U-0000")));
        assertEquals("U-1002", k.pets.findById(k.petFlow.registerPet(k.rowena, k.newPetForm("U-1002")).petId()).orElseThrow().getOwnerId());
    }

    @Test
    void notificationsBelongToTheirOwner() {
        String id = k.notifications.findByUserIdOrderByCreatedAtDesc("U-1001").get(0).getId();
        denied(404, () -> k.notifier.markRead(k.juan, id));
        k.notifier.markRead(k.maria, id);
        assertTrue(k.notifications.findById(id).orElseThrow().getRead());
    }

    @Test
    void noRoleSlipsThroughWithAMissingUser() {
        denied(403, () -> Access.require(null, "staff"));
    }

    private static AdoptionWorkflowService.Apply adoptionForm() {
        return new AdoptionWorkflowService.Apply("S-201", "Maria Santos", "09171234567", "m@test.ph", "12 Rizal St", "Balibago",
                "I love dogs", null, "Owned House", true);
    }
}
