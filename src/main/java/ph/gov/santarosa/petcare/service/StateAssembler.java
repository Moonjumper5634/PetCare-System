package ph.gov.santarosa.petcare.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.*;
import ph.gov.santarosa.petcare.eligibility.Classification;
import ph.gov.santarosa.petcare.eligibility.CriterionResult;
import ph.gov.santarosa.petcare.eligibility.EligibilityEngine;
import ph.gov.santarosa.petcare.eligibility.Result;
import ph.gov.santarosa.petcare.repository.*;

/**
 * Builds what the signed-in user is allowed to see, in exactly the shape the front end's in-memory state S has
 * (same field names), so the existing screens keep working unchanged.
 *
 *   resident : own pets, registrations, requests, adoption applications and notifications; the shelter dogs; themself
 *   staff    : every pet, registration, request and adoption; all residents; own notifications (no administrator history)
 *   admin    : everything staff sees plus all users and the eligibility criteria
 *
 * Never included: password hashes, verification codes, lock state of other accounts.
 * Values are plain strings/numbers/booleans/maps/lists so no JSON date module is needed.
 */
@Service
public class StateAssembler {

    /** Everything loaded for one user. Passed to {@link #assemble}, which has no database access. */
    public record Data(List<AppUser> users, List<Pet> pets, List<PetRegistration> registrations,
                       List<ServiceRequest> requests, List<EligibilityAssessment> assessments,
                       List<EligibilityAssessmentCriterion> assessmentCriteria, List<ServiceRecord> records,
                       List<AdoptionApplication> adoptions, List<AdoptionInterview> interviews,
                       List<ShelterPet> shelterPets, List<Notification> notifications, List<AuditLog> audit,
                       Map<String, List<EligibilityCriterion>> criteriaByService) { }

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Comparator<String> BY_ID_NUMBER = Comparator
            .comparingLong((String id) -> numberPart(id)).thenComparing(Comparator.naturalOrder());

    private final AppUserRepository users;
    private final PetRepository pets;
    private final PetRegistrationRepository registrations;
    private final ServiceRequestRepository requests;
    private final EligibilityAssessmentRepository assessments;
    private final EligibilityAssessmentCriterionRepository assessmentCriteria;
    private final ServiceRecordRepository records;
    private final AdoptionApplicationRepository adoptions;
    private final AdoptionInterviewRepository interviews;
    private final ShelterPetRepository shelterPets;
    private final NotificationRepository notifications;
    private final AuditLogRepository audit;
    private final EligibilityCriterionRepository criteria;
    private final VeterinaryServiceRepository services;
    private final Clock clock;

    public StateAssembler(AppUserRepository users, PetRepository pets, PetRegistrationRepository registrations,
                          ServiceRequestRepository requests, EligibilityAssessmentRepository assessments,
                          EligibilityAssessmentCriterionRepository assessmentCriteria, ServiceRecordRepository records,
                          AdoptionApplicationRepository adoptions, AdoptionInterviewRepository interviews,
                          ShelterPetRepository shelterPets, NotificationRepository notifications,
                          AuditLogRepository audit, EligibilityCriterionRepository criteria,
                          VeterinaryServiceRepository services, Clock clock) {
        this.users = users; this.pets = pets; this.registrations = registrations; this.requests = requests;
        this.assessments = assessments; this.assessmentCriteria = assessmentCriteria; this.records = records;
        this.adoptions = adoptions; this.interviews = interviews; this.shelterPets = shelterPets;
        this.notifications = notifications; this.audit = audit; this.criteria = criteria; this.services = services;
        this.clock = clock;
    }

    /* ------------------------------ loading (role-scoped queries) ------------------------------ */

    @Transactional(readOnly = true)
    public Map<String, Object> bootstrap(AppUser actor) {
        return assemble(actor, load(actor), LocalDate.now(clock));
    }

    Data load(AppUser actor) {
        boolean owner = Values.OWNER.equals(actor.getRole());
        boolean admin = Values.ADMIN.equals(actor.getRole());

        List<AppUser> u;
        List<Pet> p;
        List<PetRegistration> regs;
        List<ServiceRequest> reqs;
        List<AdoptionApplication> ads;
        List<AuditLog> log;
        if (owner) {
            u = List.of(actor);
            p = pets.findByOwnerId(actor.getId());
            regs = registrations.findByOwnerId(actor.getId());
            reqs = requests.findByOwnerId(actor.getId());
            ads = adoptions.findByApplicantOwnerId(actor.getId());
            log = List.of();
        } else {
            if (admin) u = users.findAll();
            else { u = new ArrayList<>(users.findByRole(Values.OWNER)); if (u.stream().noneMatch(x -> x.getId().equals(actor.getId()))) u.add(actor); }
            p = pets.findAll();
            regs = registrations.findAll();
            reqs = requests.findAll();
            ads = adoptions.findAll();
            log = admin ? audit.findTop500ByOrderByOccurredAtDesc() : List.of();
        }
        List<String> reqIds = reqs.stream().map(ServiceRequest::getId).toList();
        List<EligibilityAssessment> as = reqIds.isEmpty() ? List.of() : assessments.findByRequestIdIn(reqIds);
        List<Long> asIds = as.stream().map(EligibilityAssessment::getId).toList();
        List<EligibilityAssessmentCriterion> ac = asIds.isEmpty() ? List.of() : assessmentCriteria.findByAssessmentIdInOrderBySeq(asIds);
        List<ServiceRecord> rec = reqIds.isEmpty() ? List.of() : records.findByRequestIdIn(reqIds);
        List<String> adIds = ads.stream().map(AdoptionApplication::getId).toList();
        List<AdoptionInterview> iv = adIds.isEmpty() ? List.of() : interviews.findByAdoptionIdIn(adIds);

        Map<String, List<EligibilityCriterion>> crit = new LinkedHashMap<>();
        if (admin) for (VeterinaryService s : services.findAllByOrderBySortOrder())
            crit.put(s.getName(), criteria.findByServiceNameOrderBySortOrder(s.getName()));

        return new Data(u, p, regs, reqs, as, ac, rec, ads, iv, shelterPets.findAll(),
                notifications.findByUserIdOrderByCreatedAtDesc(actor.getId()), log, crit);
    }

    /* ------------------------------ assembling (pure) ------------------------------ */

    public Map<String, Object> assemble(AppUser actor, Data d, LocalDate today) {
        Map<String, String> actorNames = users.findAll().stream().collect(Collectors.toMap(AppUser::getId, AppUser::getFullName));
        Map<Long, List<EligibilityAssessmentCriterion>> critByAssessment = d.assessmentCriteria().stream()
                .collect(Collectors.groupingBy(EligibilityAssessmentCriterion::getAssessmentId));
        Map<String, EligibilityAssessment> assessByReq = new HashMap<>();
        for (EligibilityAssessment a : d.assessments()) assessByReq.put(a.getRequestId(), a);
        Map<String, ServiceRecord> recordByReq = new HashMap<>();
        for (ServiceRecord r : d.records()) recordByReq.put(r.getRequestId(), r);
        Map<String, AdoptionInterview> interviewByAdoption = new HashMap<>();
        for (AdoptionInterview i : d.interviews()) interviewByAdoption.put(i.getAdoptionId(), i);

        Map<String, Object> state = new LinkedHashMap<>();
        state.put("users", d.users().stream().sorted(Comparator.comparing(AppUser::getId, BY_ID_NUMBER)).map(StateAssembler::publicUser).toList());
        state.put("pets", d.pets().stream().sorted(Comparator.comparing(Pet::getId, BY_ID_NUMBER)).map(StateAssembler::pet).toList());
        state.put("registrations", d.registrations().stream().sorted(Comparator.comparing(PetRegistration::getId, BY_ID_NUMBER)).map(r -> registration(r, actorNames)).toList());
        state.put("requests", d.requests().stream().sorted(Comparator.comparing(ServiceRequest::getId, BY_ID_NUMBER))
                .map(r -> request(r, assessByReq.get(r.getId()), critByAssessment, recordByReq.get(r.getId()), actorNames)).toList());
        state.put("adoptions", d.adoptions().stream().sorted(Comparator.comparing(AdoptionApplication::getId, BY_ID_NUMBER))
                .map(a -> adoption(a, interviewByAdoption.get(a.getId()))).toList());
        state.put("shelterPets", d.shelterPets().stream().sorted(Comparator.comparing(ShelterPet::getId, BY_ID_NUMBER)).map(StateAssembler::shelterPet).toList());
        state.put("notifications", d.notifications().stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed().thenComparing(Comparator.comparing(Notification::getId, BY_ID_NUMBER).reversed()))
                .map(StateAssembler::notification).toList());
        state.put("audit", d.audit().stream()
                .sorted(Comparator.comparing(AuditLog::getOccurredAt).reversed().thenComparing(Comparator.comparing(AuditLog::getId, BY_ID_NUMBER).reversed()))
                .map(StateAssembler::auditRow).toList());
        Map<String, Object> crit = new LinkedHashMap<>();
        for (var e : d.criteriaByService().entrySet())
            crit.put(e.getKey(), e.getValue().stream().map(c -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("key", c.getCriterionKey()); m.put("label", c.getLabel()); m.put("desc", c.getDescription());
                m.put("enabled", Boolean.TRUE.equals(c.getEnabled()));
                return m;
            }).toList());
        state.put("criteria", crit);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("today", today.toString());
        out.put("user", publicUser(actor));
        out.put("state", state);
        return out;
    }

    /* ------------------------------ one function per record type ------------------------------ */

    public static Map<String, Object> publicUser(AppUser u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId()); m.put("role", u.getRole());
        m.put("firstName", u.getFirstName()); m.put("middleName", u.getMiddleName()); m.put("lastName", u.getLastName());
        m.put("name", u.getFullName()); m.put("email", u.getEmail()); m.put("mobile", u.getMobile());
        m.put("houseStreet", u.getHouseStreet()); m.put("barangay", u.getBarangay());
        m.put("city", u.getCity()); m.put("province", u.getProvince());
        m.put("status", u.getStatus()); m.put("initials", u.getInitials()); m.put("tag", u.getJobTitle());
        m.put("emailVerified", !"signup".equals(u.getVerificationPurpose()));
        m.put("mustChangePassword", Boolean.TRUE.equals(u.getMustChangePassword()));
        return m;
    }

    static Map<String, Object> pet(Pet p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId()); m.put("ownerId", p.getOwnerId()); m.put("name", p.getName()); m.put("species", p.getSpecies());
        m.put("breed", p.getBreed()); m.put("sex", p.getSex()); m.put("dob", str(p.getDob())); m.put("color", p.getColor());
        m.put("barangay", p.getBarangay()); m.put("registrationStatus", p.getRegistrationStatus());
        m.put("registeredDate", str(p.getRegisteredDate())); m.put("registrationSource", p.getRegistrationSource());
        m.put("weight", p.getWeightKg() == null ? null : p.getWeightKg().doubleValue());
        m.put("healthStatus", p.getHealthStatus()); m.put("healthNotes", p.getHealthNotes());
        m.put("rabiesStatus", p.getRabiesStatus()); m.put("lastRabies", str(p.getLastRabies()));
        m.put("dewormingStatus", p.getDewormingStatus()); m.put("lastDeworming", str(p.getLastDeworming()));
        m.put("proofOfResidency", p.getProofOfResidency());
        m.put("microchip", p.getMicrochip()); m.put("microchipStatus", p.getMicrochipStatus());
        m.put("kaponStatus", p.getKaponStatus()); m.put("kaponDate", str(p.getKaponDate()));
        m.put("photo", p.getPhotoUrl());
        return m;
    }

    static Map<String, Object> registration(PetRegistration r, Map<String,String> actorNames) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId()); m.put("petId", r.getPetId()); m.put("ownerId", r.getOwnerId()); m.put("status", r.getStatus());
        m.put("submittedDate", str(r.getSubmittedDate())); m.put("source", r.getSource()); m.put("rejectionReason", r.getRejectionReason());
        m.put("reviewedBy", r.getReviewedBy()); m.put("reviewedByName", actorNames.get(r.getReviewedBy()));
        m.put("reviewedAt", r.getReviewedAt() == null ? null : r.getReviewedAt().toString());
        return m;
    }

    static Map<String, Object> request(ServiceRequest r, EligibilityAssessment a,
                                       Map<Long, List<EligibilityAssessmentCriterion>> critByAssessment, ServiceRecord rec,
                                       Map<String,String> actorNames) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId()); m.put("petId", r.getPetId()); m.put("ownerId", r.getOwnerId()); m.put("service", r.getServiceName());
        m.put("remarks", r.getRemarks()); m.put("submittedDate", str(r.getSubmittedDate())); m.put("source", r.getSource());
        m.put("eligibility", a == null ? null : eligibility(a, critByAssessment.getOrDefault(a.getId(), List.of()), actorNames));
        m.put("staffDecision", r.getStaffDecision()); m.put("decisionDate", str(r.getDecisionDate()));
        m.put("decidedBy", r.getDecidedBy()); m.put("decidedByName", actorNames.get(r.getDecidedBy()));
        m.put("rejectionReason", r.getRejectionReason());
        m.put("serviceRecord", rec == null ? null : serviceRecord(rec));
        m.put("status", r.getStatus());
        return m;
    }

    static Map<String, Object> eligibility(EligibilityAssessment a, List<EligibilityAssessmentCriterion> rows,
                                           Map<String,String> actorNames) {
        List<Map<String, Object>> crit = new ArrayList<>();
        List<CriterionResult> typed = new ArrayList<>();
        for (EligibilityAssessmentCriterion c : rows.stream().sorted(Comparator.comparing(EligibilityAssessmentCriterion::getSeq)).toList()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("label", c.getLabel()); m.put("value", c.getActualValue()); m.put("result", c.getResult()); m.put("basis", c.getBasis());
            crit.add(m);
            typed.add(new CriterionResult(null, c.getLabel(), c.getActualValue(), Result.valueOf(c.getResult()), c.getBasis()));
        }
        Classification cls = Classification.fromLabel(a.getClassification());
        List<Map<String, Object>> path = EligibilityEngine.decisionPath(typed, cls).stream().map(n -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", n.type()); m.put("label", n.label()); m.put("value", n.value());
            m.put("result", n.result() == null ? null : n.result().name()); m.put("basis", n.basis());
            return m;
        }).toList();
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("eligible", cls == Classification.ELIGIBLE); e.put("classification", cls.label());
        e.put("criteria", crit); e.put("decisionPath", path); e.put("assessedDate", str(a.getAssessedDate()));
        e.put("assessedBy", a.getAssessedBy()); e.put("assessedByName", actorNames.get(a.getAssessedBy()));
        return e;
    }

    static Map<String, Object> serviceRecord(ServiceRecord s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("date", str(s.getRecordDate())); m.put("staff", s.getStaffName()); m.put("remarks", s.getRemarks());
        m.put("vaccineType", s.getVaccineType()); m.put("batchNumber", s.getBatchNumber()); m.put("nextDue", str(s.getNextDue()));
        m.put("microchip", s.getMicrochipNumber()); m.put("procedureDate", str(s.getProcedureDate())); m.put("postRemarks", s.getPostRemarks());
        m.put("presenterName", s.getPresenterName()); m.put("providerRole", s.getProviderRole());
        m.put("sourceType", s.getSourceType()); m.put("sourceReference", s.getSourceReference());
        m.put("serviceLocation", s.getServiceLocation());
        return m;
    }

    static Map<String, Object> adoption(AdoptionApplication a, AdoptionInterview i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId()); m.put("shelterPetId", a.getShelterPetId()); m.put("applicantOwnerId", a.getApplicantOwnerId());
        m.put("applicantName", a.getApplicantName()); m.put("contact", a.getContact()); m.put("email", a.getEmail());
        m.put("address", a.getAddress()); m.put("barangay", a.getBarangay()); m.put("reason", a.getReason());
        m.put("experience", a.getExperience()); m.put("housing", a.getHousing()); m.put("status", a.getStatus());
        m.put("interview", i == null ? null : interview(i));
        m.put("submittedDate", str(a.getSubmittedDate())); m.put("rejectionReason", a.getRejectionReason());
        return m;
    }

    static Map<String, Object> interview(AdoptionInterview i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("date", str(i.getInterviewDate())); m.put("staff", i.getStaffName()); m.put("notes", i.getNotes());
        m.put("recommendation", i.getRecommendation()); m.put("checklist", parseChecklist(i.getChecklist()));
        return m;
    }

    static List<String> parseChecklist(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return JSON.readValue(json, new TypeReference<List<String>>() { }); }
        catch (Exception e) { return List.of(); }
    }

    static String checklistJson(Collection<String> items) {
        try { return JSON.writeValueAsString(items == null ? List.of() : items); }
        catch (Exception e) { return "[]"; }
    }

    static Map<String, Object> shelterPet(ShelterPet s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("name", s.getName()); m.put("species", s.getSpecies()); m.put("breed", s.getBreed());
        m.put("sex", s.getSex()); m.put("age", s.getAgeText()); m.put("description", s.getDescription());
        m.put("temperament", s.getTemperament()); m.put("status", s.getStatus()); m.put("intake", str(s.getIntakeDate()));
        m.put("photo", s.getPhotoUrl());
        return m;
    }

    static Map<String, Object> notification(Notification n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", n.getId()); m.put("userId", n.getUserId()); m.put("title", n.getTitle()); m.put("body", n.getBody());
        m.put("date", n.getCreatedAt().toLocalDate().toString()); m.put("read", Boolean.TRUE.equals(n.getRead()));
        return m;
    }

    static Map<String, Object> auditRow(AuditLog a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId()); m.put("datetime", a.getOccurredAt().toString()); m.put("user", a.getUserName());
        m.put("action", a.getAction()); m.put("module", a.getModule()); m.put("refId", a.getRefId()); m.put("description", a.getDescription());
        return m;
    }

    private static String str(LocalDate d) { return d == null ? null : d.toString(); }

    /** "REQ-3301" -> 3301, so ids sort numerically (REQ-999 before REQ-1000). */
    static long numberPart(String id) {
        int i = id.lastIndexOf('-');
        try { return Long.parseLong(i >= 0 ? id.substring(i + 1) : id); } catch (NumberFormatException e) { return Long.MAX_VALUE; }
    }
}
