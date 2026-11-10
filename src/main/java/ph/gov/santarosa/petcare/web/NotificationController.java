package ph.gov.santarosa.petcare.web;

import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ph.gov.santarosa.petcare.security.CurrentUser;
import ph.gov.santarosa.petcare.service.Notifier;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final Notifier notifier;
    private final CurrentUser current;

    public NotificationController(Notifier notifier, CurrentUser current) { this.notifier = notifier; this.current = current; }

    @PostMapping("/{id}/read")
    public Map<String, Object> markRead(@PathVariable String id) {
        notifier.markRead(current.requireReady(), id);
        return Map.of("ok", true);
    }
}
