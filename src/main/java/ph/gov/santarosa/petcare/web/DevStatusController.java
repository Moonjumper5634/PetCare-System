package ph.gov.santarosa.petcare.web;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ph.gov.santarosa.petcare.eligibility.AssessmentResult;
import ph.gov.santarosa.petcare.eligibility.EligibilityService;
import ph.gov.santarosa.petcare.repository.*;

/**
 * DEVELOPMENT ONLY (active in the "dev" profile, removed in "prod"). Lets you check in a browser that
 * Spring Boot is really reading MySQL. It is read-only and unauthenticated; Phase 3 replaces it with the
 * real, role-protected /api.
 *
 *   GET /api/dev/db-status              rows read from every table
 *   GET /api/dev/eligibility/{petId}    decision-tree result of one pet for all three services
 */
@Profile("dev")
@RestController
@RequestMapping("/api/dev")
public class DevStatusController {

    private final AppUserRepository users;
    private final PetRepository pets;
    private final PetRegistrationRepository registrations;
    private final ServiceRequestRepository requests;
    private final EligibilityAssessmentRepository assessments;
    private final EligibilityAssessmentCriterionRepository assessmentCriteria;
    private final ServiceRecordRepository records;
    private final ShelterPetRepository shelterPets;
    private final AdoptionApplicationRepository adoptions;
    private final AdoptionInterviewRepository interviews;
    private final NotificationRepository notifications;
    private final AuditLogRepository audit;
    private final EligibilityCriterionRepository criteria;
    private final VeterinaryServiceRepository services;
    private final IdSequenceRepository sequences;
    private final EligibilityService eligibility;
    private final Clock clock;

    public DevStatusController(AppUserRepository users, PetRepository pets, PetRegistrationRepository registrations,
                               ServiceRequestRepository requests, EligibilityAssessmentRepository assessments,
                               EligibilityAssessmentCriterionRepository assessmentCriteria, ServiceRecordRepository records,
                               ShelterPetRepository shelterPets, AdoptionApplicationRepository adoptions,
                               AdoptionInterviewRepository interviews, NotificationRepository notifications,
                               AuditLogRepository audit, EligibilityCriterionRepository criteria,
                               VeterinaryServiceRepository services, IdSequenceRepository sequences,
                               EligibilityService eligibility, Clock clock) {
        this.users = users; this.pets = pets; this.registrations = registrations; this.requests = requests;
        this.assessments = assessments; this.assessmentCriteria = assessmentCriteria; this.records = records;
        this.shelterPets = shelterPets; this.adoptions = adoptions; this.interviews = interviews;
        this.notifications = notifications; this.audit = audit; this.criteria = criteria; this.services = services;
        this.sequences = sequences; this.eligibility = eligibility; this.clock = clock;
    }

    @GetMapping("/db-status")
    public Map<String, Object> dbStatus() {
        // findAll() (not count()) on purpose: it selects every mapped column, so a wrong column name fails here.
        Map<String, Object> rows = new LinkedHashMap<>();
        rows.put("app_user", users.findAll().size());
        rows.put("pet", pets.findAll().size());
        rows.put("pet_registration", registrations.findAll().size());
        rows.put("service_request", requests.findAll().size());
        rows.put("eligibility_assessment", assessments.findAll().size());
        rows.put("eligibility_assessment_criterion", assessmentCriteria.findAll().size());
        rows.put("service_record", records.findAll().size());
        rows.put("shelter_pet", shelterPets.findAll().size());
        rows.put("adoption_application", adoptions.findAll().size());
        rows.put("adoption_interview", interviews.findAll().size());
        rows.put("notification", notifications.findAll().size());
        rows.put("audit_log", audit.findAll().size());
        rows.put("eligibility_criterion", criteria.findAll().size());
        rows.put("veterinary_service", services.findAll().size());
        rows.put("id_sequence", sequences.findAll().size());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", "Spring Boot is reading MySQL");
        out.put("referenceDate", LocalDate.now(clock).toString());
        out.put("rowsRead", rows);
        return out;
    }

    @GetMapping("/eligibility/{petId}")
    public Map<String, AssessmentResult> petEligibility(@PathVariable String petId) {
        Map<String, AssessmentResult> out = new LinkedHashMap<>();
        for (var s : services.findAllByOrderBySortOrder()) out.put(s.getName(), eligibility.evaluate(petId, s.getName()));
        return out;
    }
}
