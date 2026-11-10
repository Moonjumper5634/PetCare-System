package ph.gov.santarosa.petcare.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.AdoptionApplication;
import ph.gov.santarosa.petcare.domain.AdoptionInterview;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.domain.ShelterPet;
import ph.gov.santarosa.petcare.repository.AdoptionApplicationRepository;
import ph.gov.santarosa.petcare.repository.AdoptionInterviewRepository;
import ph.gov.santarosa.petcare.repository.ShelterPetRepository;

/**
 * Adoption: resident applies, staff interview, staff approve / reject.
 * Status: Interview Required --interview--> Interview Completed --approve--> Approved ; reject from either.
 * Shelter dog: Available -> Application Pending -> Adopted (or back to Available on rejection).
 */
@Service
public class AdoptionWorkflowService {

    public record Apply(String shelterPetId, String applicantName, String contact, String email, String address,
                        String barangay, String reason, String experience, String housing, Boolean agree) { }
    public record Interview(String notes, String recommendation, List<String> checklist) { }

    private final AdoptionApplicationRepository adoptions;
    private final AdoptionInterviewRepository interviews;
    private final ShelterPetRepository shelterPets;
    private final IdGenerator ids;
    private final Notifier notifier;
    private final Clock clock;

    public AdoptionWorkflowService(AdoptionApplicationRepository adoptions, AdoptionInterviewRepository interviews,
                                   ShelterPetRepository shelterPets, IdGenerator ids, Notifier notifier,
                                   @Qualifier("clock") Clock clock) {
        this.adoptions = adoptions; this.interviews = interviews; this.shelterPets = shelterPets;
        this.ids = ids; this.notifier = notifier; this.clock = clock;
    }

    @Transactional
    public String apply(AppUser actor, Apply a) {
        Access.require(actor, Values.OWNER);
        ShelterPet dog = shelterPets.findById(a.shelterPetId() == null ? "" : a.shelterPetId())
                .orElseThrow(() -> ApiException.notFound("Dog not found."));
        if (!"Available".equals(dog.getStatus())) throw ApiException.conflict(dog.getName() + " is no longer available for adoption.");

        String name = Validation.text(a.applicantName(), "Full name", 160, true);
        String contact = Validation.phone(a.contact(), true);
        String email = Validation.email(a.email(), true);
        String address = Validation.text(a.address(), "Address", 255, true);
        String barangay = Validation.text(a.barangay(), "Barangay", 80, true);
        String reason = Validation.text(a.reason(), "Reason for adopting", 1000, true);
        String experience = Validation.text(a.experience(), "Experience with pets", 1000, false);
        String housing = Validation.oneOf(a.housing(), "Housing type", Values.HOUSING, true);
        if (!Boolean.TRUE.equals(a.agree())) throw ApiException.badRequest("You must agree to the adoption terms.");

        AdoptionApplication ap = new AdoptionApplication();
        ap.setId(ids.next("adopt"));
        ap.setShelterPetId(dog.getId());
        ap.setApplicantOwnerId(actor.getId());
        ap.setApplicantName(name); ap.setContact(contact); ap.setEmail(email); ap.setAddress(address);
        ap.setBarangay(barangay); ap.setReason(reason); ap.setExperience(experience); ap.setHousing(housing);
        ap.setStatus("Interview Required");
        ap.setSubmittedDate(LocalDate.now(clock));
        adoptions.save(ap);
        dog.setStatus("Application Pending");
        shelterPets.save(dog);
        notifier.notify(actor.getId(), "Adoption application submitted.", "Your adoption application for " + dog.getName() + " has been submitted for review.");
        notifier.audit(actor, "Create", "Adoption", ap.getId(), "Adoption application submitted for " + dog.getName() + ".");
        return ap.getId();
    }

    @Transactional
    public void recordInterview(AppUser actor, String adoptionId, Interview i) {
        Access.require(actor, Values.STAFF);
        AdoptionApplication ap = find(adoptionId);
        if (!"Interview Required".equals(ap.getStatus())) throw ApiException.conflict("The interview for this application has already been recorded.");
        String notes = Validation.text(i.notes(), "Interview notes", 2000, false);
        String rec = i.recommendation() == null || i.recommendation().isBlank() ? "Further Review"
                : Validation.oneOf(i.recommendation(), "Recommendation", Values.RECOMMENDATION, true);
        List<String> checklist = i.checklist() == null ? List.of() : i.checklist();
        if (checklist.size() > 20) throw ApiException.badRequest("Too many checklist items.");
        for (String item : checklist) Validation.text(item, "Checklist item", 120, true);
        String json = StateAssembler.checklistJson(checklist);
        if (json.length() > 1000) throw ApiException.badRequest("Checklist is too long.");

        AdoptionInterview iv = new AdoptionInterview();
        iv.setAdoptionId(ap.getId());
        iv.setInterviewDate(LocalDate.now(clock));
        iv.setStaffName(actor.getFullName());
        iv.setNotes(notes); iv.setRecommendation(rec); iv.setChecklist(json);
        interviews.save(iv);
        ap.setStatus("Interview Completed");
        adoptions.save(ap);
        notifier.notify(ap.getApplicantOwnerId(), "Adoption interview completed.",
                "Your adoption interview for this application (" + ap.getId() + ") has been completed. A final decision will follow shortly.");
        notifier.audit(actor, "Interview", "Adoption", ap.getId(), "Adoption interview completed \u2014 recommendation: " + rec + ".");
    }

    @Transactional
    public void approve(AppUser actor, String adoptionId) {
        Access.require(actor, Values.STAFF);
        AdoptionApplication ap = find(adoptionId);
        if (!"Interview Completed".equals(ap.getStatus())) throw ApiException.conflict("Complete the interview before approving this application.");
        ShelterPet dog = dogOf(ap);
        ap.setStatus("Approved");
        dog.setStatus("Adopted");
        adoptions.save(ap);
        shelterPets.save(dog);
        notifier.notify(ap.getApplicantOwnerId(), "Adoption application approved.",
                "Your adoption application for " + dog.getName() + " has been approved. Please coordinate with CVO staff for pick-up.");
        notifier.audit(actor, "Approve", "Adoption", ap.getId(), "Adoption application for " + dog.getName() + " approved \u2014 pet marked Adopted.");
    }

    @Transactional
    public void reject(AppUser actor, String adoptionId, String reason) {
        Access.require(actor, Values.STAFF);
        String why = Validation.text(reason, "Rejection reason", 1000, true);
        AdoptionApplication ap = find(adoptionId);
        if (!List.of("Interview Required", "Interview Completed").contains(ap.getStatus()))
            throw ApiException.conflict("This application has already been decided.");
        ShelterPet dog = dogOf(ap);
        ap.setStatus("Rejected");
        ap.setRejectionReason(why);
        dog.setStatus("Available");
        adoptions.save(ap);
        shelterPets.save(dog);
        notifier.notify(ap.getApplicantOwnerId(), "Adoption application rejected.",
                "Your adoption application for " + dog.getName() + " was rejected. Reason: " + why);
        notifier.audit(actor, "Reject", "Adoption", ap.getId(), "Adoption application for " + dog.getName() + " rejected \u2014 " + why);
    }

    private AdoptionApplication find(String id) {
        return adoptions.findById(id == null ? "" : id).orElseThrow(() -> ApiException.notFound("Application not found."));
    }

    private ShelterPet dogOf(AdoptionApplication ap) {
        return shelterPets.findById(ap.getShelterPetId()).orElseThrow(() -> ApiException.notFound("Dog not found."));
    }
}
