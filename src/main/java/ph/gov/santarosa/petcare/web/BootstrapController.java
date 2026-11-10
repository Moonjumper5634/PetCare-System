package ph.gov.santarosa.petcare.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ph.gov.santarosa.petcare.security.CurrentUser;
import ph.gov.santarosa.petcare.service.StateAssembler;

/** One call that gives the signed-in user everything their screens read (see StateAssembler). */
@RestController
@RequestMapping("/api")
public class BootstrapController {

    private final StateAssembler assembler;
    private final CurrentUser current;

    public BootstrapController(StateAssembler assembler, CurrentUser current) { this.assembler = assembler; this.current = current; }

    @GetMapping("/bootstrap")
    public Map<String, Object> bootstrap() { return assembler.bootstrap(current.require()); }
}
