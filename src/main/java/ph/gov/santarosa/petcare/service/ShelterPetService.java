package ph.gov.santarosa.petcare.service;

import java.time.LocalDate;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.domain.ShelterPet;
import ph.gov.santarosa.petcare.repository.ShelterPetRepository;

@Service
public class ShelterPetService {
    public record Save(String name, String breed, String sex, String ageText, String description,
                       String temperament, String status, LocalDate intakeDate, String photoUrl) { }
    private static final Set<String> MANUAL_STATUSES=Set.of("Available","Unavailable","Archived");
    private final ShelterPetRepository pets; private final IdGenerator ids; private final Notifier notifier;
    public ShelterPetService(ShelterPetRepository pets, IdGenerator ids, Notifier notifier){this.pets=pets;this.ids=ids;this.notifier=notifier;}

    @Transactional public String create(AppUser actor, Save body){
        Access.require(actor, Values.STAFF, Values.ADMIN);
        ShelterPet p=new ShelterPet(); p.setId(ids.next("shelter")); apply(p,body); pets.save(p);
        notifier.audit(actor,"Create","Shelter Dog",p.getId(),"Shelter dog listing created for "+p.getName()+"."); return p.getId();
    }
    @Transactional public void update(AppUser actor,String id,Save body){
        Access.require(actor, Values.STAFF, Values.ADMIN);
        ShelterPet p=pets.findById(id).orElseThrow(()->ApiException.notFound("Shelter dog not found."));
        apply(p,body); pets.save(p); notifier.audit(actor,"Update","Shelter Dog",p.getId(),"Shelter dog listing updated for "+p.getName()+" — "+p.getStatus()+".");
    }
    private static void apply(ShelterPet p,Save b){
        p.setName(Validation.text(b.name(),"Dog name",80,true)); p.setSpecies("Dog");
        p.setBreed(Validation.text(b.breed(),"Breed",80,false));
        String sex=Validation.text(b.sex(),"Sex",10,true); if(!Set.of("Male","Female").contains(sex)) throw ApiException.badRequest("Sex must be Male or Female."); p.setSex(sex);
        p.setAgeText(Validation.text(b.ageText(),"Age",40,false)); p.setDescription(Validation.text(b.description(),"Description",1000,false));
        p.setTemperament(Validation.text(b.temperament(),"Temperament",500,false));
        String status=Validation.text(b.status(),"Status",30,true); if(!MANUAL_STATUSES.contains(status)) throw ApiException.badRequest("Application Pending and Adopted are controlled by the adoption workflow."); p.setStatus(status);
        p.setIntakeDate(b.intakeDate()==null?LocalDate.now():b.intakeDate()); p.setPhotoUrl(Validation.text(b.photoUrl(),"Photo URL",500,false));
    }
}
