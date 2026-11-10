package ph.gov.santarosa.petcare.web;

import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ph.gov.santarosa.petcare.security.CurrentUser;
import ph.gov.santarosa.petcare.service.AdoptionWorkflowService;

@RestController
@RequestMapping("/api/adoptions")
public class AdoptionController {

    public record Reject(String reason) { }

    private final AdoptionWorkflowService adoptions;
    private final CurrentUser current;

    public AdoptionController(AdoptionWorkflowService adoptions, CurrentUser current) { this.adoptions = adoptions; this.current = current; }

    @PostMapping
    public Map<String, Object> apply(@RequestBody AdoptionWorkflowService.Apply body) {
        return Map.of("adoptionId", adoptions.apply(current.requireReady(), body));
    }

    @PostMapping("/{id}/interview")
    public Map<String, Object> interview(@PathVariable String id, @RequestBody AdoptionWorkflowService.Interview body) {
        adoptions.recordInterview(current.requireReady(), id, body);
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/approve")
    public Map<String, Object> approve(@PathVariable String id) { adoptions.approve(current.requireReady(), id); return Map.of("ok", true); }

    @PostMapping("/{id}/reject")
    public Map<String, Object> reject(@PathVariable String id, @RequestBody Reject body) {
        adoptions.reject(current.requireReady(), id, body.reason());
        return Map.of("ok", true);
    }
}
