package ph.gov.santarosa.petcare.web;

import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ph.gov.santarosa.petcare.security.CurrentUser;
import ph.gov.santarosa.petcare.service.RequestWorkflowService;

@RestController
@RequestMapping("/api/service-requests")
public class ServiceRequestController {

    public record QuickVisit(String petId, String service) { }

    private final RequestWorkflowService requests;
    private final CurrentUser current;

    public ServiceRequestController(RequestWorkflowService requests, CurrentUser current) { this.requests = requests; this.current = current; }

    @PostMapping
    public Map<String, Object> create(@RequestBody RequestWorkflowService.CreateRequest body) {
        return Map.of("requestId", requests.createRequest(current.requireReady(), body));
    }

    @PostMapping("/quick-visit")
    public Map<String, Object> quickVisit(@RequestBody QuickVisit body) {
        return Map.of("requestId", requests.quickVisit(current.requireReady(), body.petId(), body.service()));
    }

    @PostMapping("/{id}/assess")
    public Map<String, Object> assess(@PathVariable String id) { requests.assess(current.requireReady(), id); return Map.of("ok", true); }

    @PostMapping("/{id}/decision")
    public Map<String, Object> decide(@PathVariable String id, @RequestBody RequestWorkflowService.Decision body) {
        requests.decide(current.requireReady(), id, body);
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/record")
    public Map<String, Object> record(@PathVariable String id, @RequestBody RequestWorkflowService.RecordService body) {
        requests.recordService(current.requireReady(), id, body);
        return Map.of("ok", true);
    }
}
