package ph.gov.santarosa.petcare.eligibility;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.Pet;
import ph.gov.santarosa.petcare.repository.EligibilityCriterionRepository;
import ph.gov.santarosa.petcare.repository.PetRepository;
import ph.gov.santarosa.petcare.repository.VeterinaryServiceRepository;

/**
 * Database-backed facade over {@link EligibilityEngine}; the server-side counterpart of the front end's
 * EligibilityService.evaluate() (README: POST /api/eligibility/evaluate). It reads the pet and the ENABLED
 * criteria for the service from MySQL and runs the decision tree. It does not save anything: recording an
 * assessment against a request belongs to the Phase 3 workflow.
 */
@Service
public class EligibilityService {

    private final EligibilityEngine engine = new EligibilityEngine();
    private final PetRepository pets;
    private final EligibilityCriterionRepository criteria;
    private final VeterinaryServiceRepository services;
    private final Clock clock;

    public EligibilityService(PetRepository pets, EligibilityCriterionRepository criteria,
                              VeterinaryServiceRepository services, Clock clock) {
        this.pets = pets;
        this.criteria = criteria;
        this.services = services;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AssessmentResult evaluate(String petId, String service) {
        Pet pet = pets.findById(petId).orElseThrow(() -> new IllegalArgumentException("Unknown pet: " + petId));
        return evaluate(pet, service);
    }

    /** Same as above for a pet the caller already loaded. */
    @Transactional(readOnly = true)
    public AssessmentResult evaluate(Pet pet, String service) {
        if (!services.existsById(service)) throw new IllegalArgumentException("Unknown service: " + service);
        List<String> enabledKeys = criteria.findByServiceNameAndEnabledTrueOrderBySortOrder(service).stream()
                .map(c -> c.getCriterionKey()).toList();
        return engine.assess(factsOf(pet), service, enabledKeys, LocalDate.now(clock));
    }

    public static PetFacts factsOf(Pet p) {
        return new PetFacts(p.getRegistrationStatus(), p.getBreed(), p.getColor(), p.getDob(), p.getWeightKg(),
                p.getHealthStatus(), p.getRabiesStatus(), p.getLastRabies(), p.getDewormingStatus(),
                p.getLastDeworming(), p.getProofOfResidency(), p.getMicrochip(), p.getMicrochipStatus());
    }
}
