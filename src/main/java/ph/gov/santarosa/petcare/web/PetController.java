package ph.gov.santarosa.petcare.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ph.gov.santarosa.petcare.eligibility.AssessmentResult;
import ph.gov.santarosa.petcare.security.CurrentUser;
import ph.gov.santarosa.petcare.service.PetWorkflowService;
import ph.gov.santarosa.petcare.service.RequestWorkflowService;

@RestController
@RequestMapping("/api")
public class PetController {

    public record VerifyBody(Boolean onSite) { }
    public record RejectBody(String reason) { }

    private final PetWorkflowService pets;
    private final RequestWorkflowService requests;
    private final CurrentUser current;

    public PetController(PetWorkflowService pets, RequestWorkflowService requests, CurrentUser current) {
        this.pets = pets; this.requests = requests; this.current = current;
    }

    @PostMapping("/pets")
    public Map<String, Object> register(@RequestBody PetWorkflowService.RegisterPet body) {
        PetWorkflowService.Registered r = pets.registerPet(current.requireReady(), body);
        return Map.of("petId", r.petId(), "registrationId", r.registrationId());
    }

    @GetMapping("/pets/{petId}/eligibility-preview")
    public AssessmentResult preview(@PathVariable String petId, @RequestParam("service") String service) {
        return requests.preview(current.requireReady(), petId, service);
    }

    @PostMapping("/registrations/{id}/verify")
    public Map<String, Object> verify(@PathVariable String id, @RequestBody(required = false) VerifyBody body) {
        pets.verifyRegistration(current.requireReady(), id, body != null && Boolean.TRUE.equals(body.onSite()));
        return Map.of("ok", true);
    }

    @PostMapping("/registrations/{id}/reject")
    public Map<String, Object> reject(@PathVariable String id, @RequestBody RejectBody body) {
        pets.rejectRegistration(current.requireReady(), id, body.reason());
        return Map.of("ok", true);
    }

    @PostMapping("/registrations/{id}/resubmit")
    public Map<String,Object> resubmit(@PathVariable String id, @RequestBody PetWorkflowService.RegisterPet body){
        pets.resubmitRegistration(current.requireReady(),id,body); return Map.of("registrationId",id);
    }
}
