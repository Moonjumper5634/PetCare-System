package ph.gov.santarosa.petcare.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ph.gov.santarosa.petcare.support.Kit;

class SurrenderWorkflowServiceTest {
    private Kit k;
    @BeforeEach void setUp(){k=new Kit();}
    private SurrenderWorkflowService.Submit form(){return new SurrenderWorkflowService.Submit("P-1042","Owner can no longer provide care","Healthy and friendly","09171234567",true);}

    @Test void residentSubmissionIsPrivateAndNotifiesStaff(){
        String id=k.surrenderFlow.submit(k.maria,form());
        var r=k.surrenders.findById(id).orElseThrow();
        assertEquals("Submitted",r.getStatus()); assertEquals(k.maria.getId(),r.getOwnerId());
        assertFalse(k.notifications.findByUserIdOrderByCreatedAtDesc(k.rowena.getId()).isEmpty());
        assertEquals(409,assertThrows(ApiException.class,()->k.surrenderFlow.submit(k.maria,form())).getStatus());
        assertEquals(404,assertThrows(ApiException.class,()->k.surrenderFlow.submit(k.juan,form())).getStatus());
    }

    @Test void staffRequestsCorrectionAndResidentResubmits(){
        String id=k.surrenderFlow.submit(k.maria,form());
        k.surrenderFlow.act(k.rowena,id,new SurrenderWorkflowService.StaffAction("More Information Needed","Add bite history",null));
        assertEquals("More Information Needed",k.surrenders.findById(id).orElseThrow().getStatus());
        k.surrenderFlow.resubmit(k.maria,id,new SurrenderWorkflowService.Resubmit("Relocating","No bite history","09171234567"));
        assertEquals("Submitted",k.surrenders.findById(id).orElseThrow().getStatus());
    }

    @Test void handoverCreatesAnUnpublishedDogThenStaffPublishesIt(){
        String id=k.surrenderFlow.submit(k.maria,form());
        k.surrenderFlow.act(k.rowena,id,new SurrenderWorkflowService.StaffAction("Handover Scheduled","Bring vaccination record","2026-08-20"));
        k.surrenderFlow.act(k.rowena,id,new SurrenderWorkflowService.StaffAction("Received","Identity and records checked",null));
        var r=k.surrenders.findById(id).orElseThrow();
        assertEquals("Under Assessment",k.shelterPets.findById(r.getShelterPetId()).orElseThrow().getStatus());
        k.surrenderFlow.act(k.rowena,id,new SurrenderWorkflowService.StaffAction("Available for Adoption","Cleared for listing",null));
        assertEquals("Available",k.shelterPets.findById(r.getShelterPetId()).orElseThrow().getStatus());
    }
}
