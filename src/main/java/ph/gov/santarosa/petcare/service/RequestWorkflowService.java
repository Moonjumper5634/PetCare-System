package ph.gov.santarosa.petcare.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.domain.EligibilityAssessment;
import ph.gov.santarosa.petcare.domain.EligibilityAssessmentCriterion;
import ph.gov.santarosa.petcare.domain.Pet;
import ph.gov.santarosa.petcare.domain.ServiceRecord;
import ph.gov.santarosa.petcare.domain.ServiceRequest;
import ph.gov.santarosa.petcare.eligibility.AssessmentResult;
import ph.gov.santarosa.petcare.eligibility.CriterionResult;
import ph.gov.santarosa.petcare.eligibility.EligibilityService;
import ph.gov.santarosa.petcare.eligibility.ServiceNames;
import ph.gov.santarosa.petcare.repository.EligibilityAssessmentCriterionRepository;
import ph.gov.santarosa.petcare.repository.EligibilityAssessmentRepository;
import ph.gov.santarosa.petcare.repository.PetRepository;
import ph.gov.santarosa.petcare.repository.ServiceRecordRepository;
import ph.gov.santarosa.petcare.repository.ServiceRequestRepository;
import ph.gov.santarosa.petcare.repository.VeterinaryServiceRepository;

/**
 * Service requests: submit, eligibility assessment, staff decision, service record, QR quick visit.
 *
 * Request life cycle (status / staffDecision):
 *   Submitted/Pending --assess--> For Review/Pending --decide--> Approved|Rejected  (or For Review/Verification Requested)
 *   Approved --record service--> Completed
 */
@Service
public class RequestWorkflowService {

    public record CreateRequest(String petId, String service, String remarks) { }
    public record Decision(String decision, String reason) { }
    public record RecordService(String serviceDate, String staffName, String remarks, String vaccineType,
                                String batchNumber, String nextDue, String microchipNumber, String procedureDate,
                                String postRemarks, String presenterName, String providerRole, String sourceType,
                                String sourceReference, String serviceLocation) { }

    private final ServiceRequestRepository requests;
    private final PetRepository pets;
    private final VeterinaryServiceRepository services;
    private final EligibilityAssessmentRepository assessments;
    private final EligibilityAssessmentCriterionRepository assessmentCriteria;
    private final ServiceRecordRepository records;
    private final EligibilityService eligibility;
    private final IdGenerator ids;
    private final Notifier notifier;
    private final Clock clock;

    public RequestWorkflowService(ServiceRequestRepository requests, PetRepository pets,
                                  VeterinaryServiceRepository services, EligibilityAssessmentRepository assessments,
                                  EligibilityAssessmentCriterionRepository assessmentCriteria, ServiceRecordRepository records,
                                  EligibilityService eligibility, IdGenerator ids, Notifier notifier,
                                  @Qualifier("clock") Clock clock) {
        this.requests = requests; this.pets = pets; this.services = services; this.assessments = assessments;
        this.assessmentCriteria = assessmentCriteria; this.records = records; this.eligibility = eligibility;
        this.ids = ids; this.notifier = notifier; this.clock = clock;
    }

    /* ------------------------------ submit ------------------------------ */

    @Transactional
    public String createRequest(AppUser actor, CreateRequest r) {
        Access.require(actor, Values.OWNER, Values.STAFF);
        Pet pet = petFor(actor, r.petId());
        if (!"Verified".equals(pet.getRegistrationStatus()))
            throw ApiException.badRequest("Only verified pets can request a service.");
        String service = serviceName(r.service());
        String remarks = Validation.text(r.remarks(), "Remarks", 1000, false);
        if (requests.existsByPetIdAndServiceNameAndStatusIn(pet.getId(), service, Values.OPEN_REQUEST))
            throw ApiException.conflict("There is already an open " + service + " request for " + pet.getName() + ".");

        ServiceRequest req = new ServiceRequest();
        req.setId(ids.next("req"));
        req.setPetId(pet.getId());
        req.setOwnerId(pet.getOwnerId());
        req.setServiceName(service);
        req.setRemarks(remarks);
        req.setSubmittedDate(LocalDate.now(clock));
        req.setSource(Values.STAFF.equals(actor.getRole()) ? "Walk-in" : "Owner");
        req.setStaffDecision("Pending");
        req.setStatus("Submitted");
        requests.save(req);
        notifier.notify(pet.getOwnerId(), "Service request submitted.", "Your " + service + " request for " + pet.getName() + " has been submitted.");
        notifier.audit(actor, "Create", "Service Request", req.getId(), service + " request submitted for " + pet.getName() + ".");
        return req.getId();
    }

    /** QR identifies the pet and starts a request; it never bypasses assessment or staff decision. */
    @Transactional
    public String quickVisit(AppUser actor, String petId, String service) {
        Access.require(actor, Values.STAFF);
        Pet pet = pets.findById(petId == null ? "" : petId).orElseThrow(() -> ApiException.notFound("Pet not found."));
        if (!"Verified".equals(pet.getRegistrationStatus()))
            throw ApiException.badRequest("Only verified pets can receive a service.");
        String svc = serviceName(service);
        ServiceRequest req = new ServiceRequest();
        req.setId(ids.next("req"));
        req.setPetId(pet.getId());
        req.setOwnerId(pet.getOwnerId());
        req.setServiceName(svc);
        req.setRemarks("Recorded via QR scan (in-person visit).");
        req.setSubmittedDate(LocalDate.now(clock));
        req.setSource("QR Scan");
        req.setStaffDecision("Pending");
        req.setStatus("Submitted");
        requests.save(req);
        notifier.audit(actor, "Create", "Service Request", req.getId(),
                svc + " visit for " + pet.getName() + " started via QR scan; eligibility review required.");
        return req.getId();
    }

    /* ------------------------------ eligibility ------------------------------ */

    /** Runs the decision tree with the currently enabled criteria and stores the result on the request. */
    @Transactional
    public void assess(AppUser actor, String requestId) {
        Access.require(actor, Values.STAFF);
        ServiceRequest req = undecided(requestId);
        Pet pet = pets.findById(req.getPetId()).orElseThrow(() -> ApiException.notFound("Pet not found."));
        AssessmentResult result = eligibility.evaluate(pet, req.getServiceName());

        EligibilityAssessment a = assessments.findByRequestId(req.getId()).orElse(null);
        boolean fresh = a == null;
        if (fresh) a = new EligibilityAssessment();
        a.setRequestId(req.getId());
        a.setClassification(result.classification().label());
        a.setAssessedDate(result.assessedDate());
        a.setAssessedBy(actor.getId());
        a = assessments.save(a);
        if (!fresh) assessmentCriteria.deleteByAssessmentId(a.getId());   // runs immediately; see the repository
        int seq = 1;
        for (CriterionResult c : result.criteria()) {
            EligibilityAssessmentCriterion row = new EligibilityAssessmentCriterion();
            row.setAssessmentId(a.getId());
            row.setSeq(seq++);
            row.setLabel(c.label());
            row.setActualValue(c.value());
            row.setResult(c.result().name());
            row.setBasis(c.basis());
            assessmentCriteria.save(row);
        }
        req.setStatus("For Review");
        requests.save(req);
        String cls = result.classification().label();
        notifier.notify(req.getOwnerId(), "Eligibility assessment completed.",
                "An eligibility assessment for " + pet.getName() + "'s " + req.getServiceName()
                        + " request has been completed. System recommendation: " + cls + ".");
        notifier.audit(actor, "Assess", "Eligibility Assessment", req.getId(),
                "Ran eligibility assessment for " + pet.getName() + " \u2014 " + req.getServiceName() + " \u2014 " + cls + ".");
    }

    /** Preview shown to a resident (own pet) or staff while filling in the request form. Saves nothing. */
    @Transactional(readOnly = true)
    public AssessmentResult preview(AppUser actor, String petId, String service) {
        Access.require(actor, Values.OWNER, Values.STAFF);
        Pet pet = petFor(actor, petId);
        return eligibility.evaluate(pet, serviceName(service));
    }

    /* ------------------------------ staff decision ------------------------------ */

    @Transactional
    public void decide(AppUser actor, String requestId, Decision d) {
        Access.require(actor, Values.STAFF);
        ServiceRequest req = undecided(requestId);
        Pet pet = pets.findById(req.getPetId()).orElseThrow(() -> ApiException.notFound("Pet not found."));
        String kind = d.decision() == null ? "" : d.decision();
        LocalDate today = LocalDate.now(clock);
        switch (kind) {
            case "Approved" -> {
                if (assessments.findByRequestId(req.getId()).isEmpty())
                    throw ApiException.badRequest("Run the eligibility assessment before approving this request.");
                req.setStaffDecision("Approved"); req.setStatus("Approved");
                notifier.notify(req.getOwnerId(), "Request approved.", "Your " + req.getServiceName() + " request (" + req.getId() + ") for "
                        + pet.getName() + " has been approved. Please wait for the service to be scheduled and recorded.");
                notifier.audit(actor, "Approve", "Service Request", req.getId(), req.getServiceName() + " request for " + pet.getName() + " approved.");
            }
            case "Rejected" -> {
                String why = Validation.text(d.reason(), "Rejection reason", 1000, true);
                req.setStaffDecision("Rejected"); req.setStatus("Rejected"); req.setRejectionReason(why);
                notifier.notify(req.getOwnerId(), "Request rejected.", "Your " + req.getServiceName() + " request (" + req.getId() + ") for "
                        + pet.getName() + " was rejected. Reason: " + why);
                notifier.audit(actor, "Reject", "Service Request", req.getId(), req.getServiceName() + " request for " + pet.getName() + " rejected \u2014 " + why);
            }
            case "Verification Requested" -> {
                req.setStaffDecision("Verification Requested"); req.setStatus("For Review");
                notifier.notify(req.getOwnerId(), "Additional verification requested.", "CVO staff requested additional verification for your "
                        + req.getServiceName() + " request (" + req.getId() + ") for " + pet.getName() + " before a final decision can be made.");
                notifier.audit(actor, "Request Verification", "Service Request", req.getId(),
                        req.getServiceName() + " request for " + pet.getName() + " \u2014 additional verification requested from resident.");
            }
            default -> throw ApiException.badRequest("Unknown decision.");
        }
        req.setDecisionDate(today);
        req.setDecidedBy(actor.getId());
        requests.save(req);
    }

    /* ------------------------------ service record ------------------------------ */

    @Transactional
    public void recordService(AppUser actor, String requestId, RecordService r) {
        Access.require(actor, Values.STAFF);
        ServiceRequest req = requests.findById(requestId == null ? "" : requestId).orElseThrow(() -> ApiException.notFound("Request not found."));
        if (!"Approved".equals(req.getStatus()) || !"Approved".equals(req.getStaffDecision()))
            throw ApiException.conflict("Only approved requests can be recorded as completed.");
        if (records.findByRequestId(req.getId()).isPresent()) throw ApiException.conflict("A service record already exists for this request.");
        Pet pet = pets.findById(req.getPetId()).orElseThrow(() -> ApiException.notFound("Pet not found."));
        LocalDate today = LocalDate.now(clock);

        LocalDate date = Validation.pastOrToday(r.serviceDate(), "Service date", false, today);
        if (date == null) date = today;
        String staff = Validation.text(r.staffName(), "Staff / veterinarian", 120, false);
        if (staff == null) staff = actor.getFullName();

        ServiceRecord rec = new ServiceRecord();
        rec.setRequestId(req.getId());
        rec.setRecordDate(date);
        rec.setStaffName(staff);
        rec.setRemarks(Validation.text(r.remarks(), "Remarks", 1000, false));
        rec.setPresenterName(Validation.text(r.presenterName(), "Person presenting the pet", 160, false));
        rec.setProviderRole(Validation.text(r.providerRole(), "Service provider role", 80, false));
        String source = Validation.text(r.sourceType(), "Record source", 40, false);
        rec.setSourceType(source == null ? "Digital request" : source);
        rec.setSourceReference(Validation.text(r.sourceReference(), "Source reference", 120, false));
        rec.setServiceLocation(Validation.text(r.serviceLocation(), "Service location", 160, false));

        switch (req.getServiceName()) {
            case ServiceNames.ANTI_RABIES -> {
                rec.setVaccineType(Validation.text(r.vaccineType(), "Vaccine type", 80, false));
                rec.setBatchNumber(Validation.text(r.batchNumber(), "Batch / lot number", 60, false));
                rec.setNextDue(Validation.date(r.nextDue(), "Next due date", false));
                pet.setLastRabies(date);
                pet.setRabiesStatus("Updated");
            }
            case ServiceNames.MICROCHIPPING -> {
                String chip = Validation.microchip(r.microchipNumber());
                if (chip == null) throw ApiException.badRequest("Microchip number is required.");
                if ("Microchipped".equals(pet.getMicrochipStatus()) || pet.getMicrochip() != null)
                    throw ApiException.conflict(pet.getName() + " already has a microchip on file.");
                if (pets.findByMicrochip(chip).isPresent()) throw ApiException.conflict("That microchip number is already registered to another pet.");
                rec.setMicrochipNumber(chip);
                pet.setMicrochip(chip);
                pet.setMicrochipStatus("Microchipped");
            }
            default -> {   // Kapon / Spay-Neuter
                LocalDate proc = Validation.pastOrToday(r.procedureDate(), "Procedure date", false, today);
                rec.setProcedureDate(proc);
                rec.setPostRemarks(Validation.text(r.postRemarks(), "Post-procedure remarks", 1000, false));
                pet.setKaponStatus("Completed");
                pet.setKaponDate(proc != null ? proc : date);
            }
        }
        records.save(rec);
        pets.save(pet);
        req.setStatus("Completed");
        requests.save(req);
        notifier.notify(req.getOwnerId(), "Service completed.", req.getServiceName() + " for " + pet.getName() + " has been completed and recorded by CVO staff.");
        notifier.audit(actor, "Create", "Service Record", req.getId(), req.getServiceName() + " recorded for " + pet.getName() + ".");
    }

    /* ------------------------------ helpers ------------------------------ */

    /** A resident only ever "finds" their own pets; someone else's pet looks exactly like a missing one. */
    private Pet petFor(AppUser actor, String petId) {
        Pet pet = pets.findById(petId == null ? "" : petId).orElseThrow(() -> ApiException.notFound("Pet not found."));
        if (Values.OWNER.equals(actor.getRole()) && !pet.getOwnerId().equals(actor.getId())) throw ApiException.notFound("Pet not found.");
        return pet;
    }

    private String serviceName(String service) {
        String s = Validation.text(service, "Service", 40, true);
        if (!services.existsById(s)) throw ApiException.badRequest("Unknown service.");
        return s;
    }

    /** The request exists and no final decision has been made yet. */
    private ServiceRequest undecided(String requestId) {
        ServiceRequest req = requests.findById(requestId == null ? "" : requestId).orElseThrow(() -> ApiException.notFound("Request not found."));
        boolean open = List.of("Submitted", "For Review").contains(req.getStatus())
                && List.of("Pending", "Verification Requested").contains(req.getStaffDecision());
        if (!open) throw ApiException.conflict("This request has already been decided.");
        return req;
    }
}
