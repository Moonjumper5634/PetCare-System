package ph.gov.santarosa.petcare.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ph.gov.santarosa.petcare.security.CurrentUser;
import ph.gov.santarosa.petcare.service.SettingsService;

@RestController @RequestMapping("/api/admin/settings")
public class SettingsController {
    private final SettingsService settings; private final CurrentUser current;
    public SettingsController(SettingsService settings, CurrentUser current){this.settings=settings;this.current=current;}
    @GetMapping public Map<String,Object> read(){return settings.read(current.requireReady());}
    @PutMapping public Map<String,Object> update(@RequestBody SettingsService.Update body){settings.update(current.requireReady(),body);return Map.of("ok",true);}
}
