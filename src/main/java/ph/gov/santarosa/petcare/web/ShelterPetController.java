package ph.gov.santarosa.petcare.web;

import java.util.Map;
import org.springframework.web.bind.annotation.*;
import ph.gov.santarosa.petcare.security.CurrentUser;
import ph.gov.santarosa.petcare.service.ShelterPetService;

@RestController @RequestMapping("/api/shelter-pets")
public class ShelterPetController {
    private final ShelterPetService service; private final CurrentUser current;
    public ShelterPetController(ShelterPetService service,CurrentUser current){this.service=service;this.current=current;}
    @PostMapping public Map<String,Object> create(@RequestBody ShelterPetService.Save body){return Map.of("id",service.create(current.requireReady(),body));}
    @PutMapping("/{id}") public Map<String,Object> update(@PathVariable String id,@RequestBody ShelterPetService.Save body){service.update(current.requireReady(),id,body);return Map.of("ok",true);}
}
