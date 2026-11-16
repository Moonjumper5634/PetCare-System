package ph.gov.santarosa.petcare.web;

import java.util.Map;
import org.springframework.web.bind.annotation.*;
import ph.gov.santarosa.petcare.security.CurrentUser;
import ph.gov.santarosa.petcare.service.SurrenderWorkflowService;

@RestController @RequestMapping("/api/surrenders")
public class SurrenderController {
    private final SurrenderWorkflowService flow; private final CurrentUser current;
    public SurrenderController(SurrenderWorkflowService flow,CurrentUser current){this.flow=flow;this.current=current;}
    @PostMapping public Map<String,Object> submit(@RequestBody SurrenderWorkflowService.Submit b){return Map.of("surrenderId",flow.submit(current.requireReady(),b));}
    @PostMapping("/{id}/resubmit") public Map<String,Object> resubmit(@PathVariable String id,@RequestBody SurrenderWorkflowService.Resubmit b){flow.resubmit(current.requireReady(),id,b);return Map.of("ok",true);}
    @PostMapping("/{id}/action") public Map<String,Object> action(@PathVariable String id,@RequestBody SurrenderWorkflowService.StaffAction b){flow.act(current.requireReady(),id,b);return Map.of("ok",true);}
}
