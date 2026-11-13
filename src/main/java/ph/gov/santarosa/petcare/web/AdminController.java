package ph.gov.santarosa.petcare.web;

import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ph.gov.santarosa.petcare.security.CurrentUser;
import ph.gov.santarosa.petcare.service.AdminService;

@RestController
@RequestMapping("/api")
public class AdminController {

    public record Status(String status) { }
    public record Criterion(String service, String key, Boolean enabled, String label, String description) { }

    private final AdminService admin;
    private final CurrentUser current;

    public AdminController(AdminService admin, CurrentUser current) {
        this.admin = admin; this.current = current;
    }

    /** Staff: create a resident account at the walk-in counter. */
    @PostMapping("/residents")
    public Map<String, Object> createResident(@RequestBody AdminService.CreateResident body) {
        AdminService.ResidentCreated r = admin.createResident(current.requireReady(), body);
        return Map.of("userId", r.userId(), "email", r.email(), "temporaryPassword", r.temporaryPassword());
    }

    @PostMapping("/admin/personnel")
    public Map<String, Object> createPersonnel(@RequestBody AdminService.CreatePersonnel body) {
        AdminService.PersonnelCreated r = admin.createPersonnel(current.requireReady(), body);
        return Map.of("userId", r.userId(), "email", r.email(),
                "temporaryPassword", r.temporaryPassword(), "role", r.role());
    }

    @PostMapping("/admin/users/{id}/status")
    public Map<String, Object> setStatus(@PathVariable String id, @RequestBody Status body) {
        admin.setUserStatus(current.requireReady(), id, body.status());
        return Map.of("ok", true);
    }

    @PutMapping("/admin/criteria")
    public Map<String, Object> updateCriterion(@RequestBody Criterion body) {
        admin.updateCriterion(current.requireReady(), body.service(), body.key(), new AdminService.CriterionUpdate(body.enabled(), body.label(), body.description()));
        return Map.of("ok", true);
    }

    @PostMapping("/admin/criteria")
    public Map<String,Object> addCriterion(@RequestBody AdminService.NewCriterion body) {
        admin.addCriterion(current.requireReady(), body); return Map.of("ok",true);
    }
}
