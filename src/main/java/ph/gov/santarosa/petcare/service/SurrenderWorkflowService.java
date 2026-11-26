package ph.gov.santarosa.petcare.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.*;
import ph.gov.santarosa.petcare.repository.*;

@Service
public class SurrenderWorkflowService {
    public record Submit(String petId, String reason, String circumstances, String contact, Boolean acknowledge) { }
    public record Resubmit(String reason, String circumstances, String contact) { }
    public record StaffAction(String action, String notes, String handoverDate) { }
    private static final List<String> OPEN = List.of("Submitted","Under Review","More Information Needed","Handover Scheduled");

    private final SurrenderRequestRepository requests; private final PetRepository pets;
    private final ShelterPetRepository shelterPets; private final AppUserRepository users;
    private final IdGenerator ids; private final Notifier notifier; private final Clock clock;

    public SurrenderWorkflowService(SurrenderRequestRepository requests, PetRepository pets,
            ShelterPetRepository shelterPets, AppUserRepository users, IdGenerator ids, Notifier notifier,
            @Qualifier("clock") Clock clock) {
        this.requests=requests; this.pets=pets; this.shelterPets=shelterPets; this.users=users;
        this.ids=ids; this.notifier=notifier; this.clock=clock;
    }

    @Transactional
    public String submit(AppUser actor, Submit body) {
        Access.require(actor, Values.OWNER);
        Pet pet=ownedPet(actor, body.petId());
        if(requests.existsByPetIdAndStatusIn(pet.getId(),OPEN)) throw ApiException.conflict("This dog already has an active surrender request.");
        if(!Boolean.TRUE.equals(body.acknowledge())) throw ApiException.badRequest("Confirm that this is only a request for CVO assessment.");
        SurrenderRequest r=new SurrenderRequest(); r.setId(ids.next("surrender")); r.setOwnerId(actor.getId()); r.setPetId(pet.getId());
        r.setReason(Validation.text(body.reason(),"Reason for surrender",1000,true));
        r.setCircumstances(Validation.text(body.circumstances(),"Care and behavior information",1000,false));
        r.setContact(Validation.phone(body.contact(),true)); r.setStatus("Submitted"); r.setSubmittedDate(LocalDate.now(clock)); r.setSource("Resident Web");
        requests.save(r);
        notifier.notify(actor.getId(),"Surrender request received.","Your request "+r.getId()+" for "+pet.getName()+" is under CVO review. Keep the dog in your care until staff confirm a handover.");
        notifyStaff("New surrender request.",r.getId()+" for "+pet.getName()+" requires review.");
        notifier.audit(actor,"Create","Surrender Request",r.getId(),"Resident submitted a surrender assessment request for "+pet.getName()+".");
        return r.getId();
    }

    @Transactional
    public void resubmit(AppUser actor,String id,Resubmit body){
        Access.require(actor,Values.OWNER); SurrenderRequest r=find(id);
        if(!actor.getId().equals(r.getOwnerId())) throw ApiException.notFound("Surrender request not found.");
        if(!"More Information Needed".equals(r.getStatus())) throw ApiException.conflict("This request is not awaiting resident information.");
        r.setReason(Validation.text(body.reason(),"Reason for surrender",1000,true));
        r.setCircumstances(Validation.text(body.circumstances(),"Care and behavior information",1000,false));
        r.setContact(Validation.phone(body.contact(),true)); r.setStatus("Submitted"); requests.save(r);
        notifyStaff("Surrender request updated.",r.getId()+" was corrected and resubmitted by the resident.");
        notifier.audit(actor,"Resubmit","Surrender Request",r.getId(),"Resident supplied requested surrender information.");
    }

    @Transactional
    public void act(AppUser actor,String id,StaffAction body){
        Access.require(actor,Values.STAFF); SurrenderRequest r=find(id); Pet pet=pets.findById(r.getPetId()).orElseThrow();
        String action=body.action()==null?"":body.action(); String notes=Validation.text(body.notes(),"Staff notes",2000,false);
        switch(action){
            case "Under Review" -> requireStatus(r,List.of("Submitted"));
            case "More Information Needed" -> { requireStatus(r,List.of("Submitted","Under Review")); if(notes==null) throw ApiException.badRequest("Explain what information is needed."); }
            case "Handover Scheduled" -> { requireStatus(r,List.of("Submitted","Under Review")); r.setHandoverDate(Validation.date(body.handoverDate(),"Handover date",true)); }
            case "Declined" -> { requireStatus(r,OPEN); if(notes==null) throw ApiException.badRequest("A decline reason is required."); }
            case "Received" -> { requireStatus(r,List.of("Handover Scheduled")); receive(r,pet); }
            case "Available for Adoption" -> { requireStatus(r,List.of("Received")); publish(r); }
            default -> throw ApiException.badRequest("Unknown surrender action.");
        }
        r.setStatus(action); r.setStaffNotes(notes); r.setReviewedBy(actor.getId()); r.setReviewedAt(notifier.now()); requests.save(r);
        String message=switch(action){
            case "More Information Needed" -> "CVO staff need more information for "+r.getId()+": "+notes;
            case "Handover Scheduled" -> "CVO staff scheduled the assessment/handover for "+r.getHandoverDate()+". Bring the dog only at the confirmed time.";
            case "Declined" -> "Your surrender request "+r.getId()+" was declined. Reason: "+notes;
            case "Received" -> "CVO staff recorded receipt of "+pet.getName()+". Adoption availability still requires staff assessment.";
            case "Available for Adoption" -> pet.getName()+" has completed staff assessment and is now listed for adoption.";
            default -> "Your surrender request "+r.getId()+" is now "+action+".";
        };
        notifier.notify(r.getOwnerId(),"Surrender request updated.",message);
        notifier.audit(actor,"Update","Surrender Request",r.getId(),pet.getName()+" — "+action+(notes==null?"":" — "+notes));
    }

    private void receive(SurrenderRequest r,Pet p){
        ShelterPet s=new ShelterPet(); s.setId(ids.next("shelter")); s.setName(p.getName()); s.setSpecies(p.getSpecies()); s.setBreed(p.getBreed());
        s.setSex(p.getSex()); s.setAgeText(p.getDob()==null?"For assessment":Math.max(0,java.time.Period.between(p.getDob(),LocalDate.now(clock)).getYears())+" years");
        s.setDescription(r.getCircumstances()); s.setTemperament("For staff assessment"); s.setStatus("Under Assessment"); s.setIntakeDate(LocalDate.now(clock)); s.setPhotoUrl(p.getPhotoUrl());
        shelterPets.save(s); r.setShelterPetId(s.getId());
        // Preserve the resident record for history, but remove it from every active-owner workflow.
        // The shelter record becomes the operational record after CVO confirms physical receipt.
        p.setArchived(Boolean.TRUE);
        p.setArchivedAt(notifier.now());
        pets.save(p);
    }
    private void publish(SurrenderRequest r){
        ShelterPet s=shelterPets.findById(r.getShelterPetId()).orElseThrow(()->ApiException.notFound("Intake dog not found.")); s.setStatus("Available"); shelterPets.save(s);
    }
    private void notifyStaff(String title,String body){ users.findByRole(Values.STAFF).stream().filter(u->"Active".equals(u.getStatus())).forEach(u->notifier.notify(u.getId(),title,body)); }
    private Pet ownedPet(AppUser a,String id){ Pet p=pets.findById(id==null?"":id).orElseThrow(()->ApiException.notFound("Dog not found.")); if(!a.getId().equals(p.getOwnerId()) || Boolean.TRUE.equals(p.getArchived())) throw ApiException.notFound("Dog not found."); return p; }
    private SurrenderRequest find(String id){return requests.findById(id==null?"":id).orElseThrow(()->ApiException.notFound("Surrender request not found."));}
    private static void requireStatus(SurrenderRequest r,List<String> allowed){if(!allowed.contains(r.getStatus())) throw ApiException.conflict("That action is not available while the request is "+r.getStatus()+".");}
}
