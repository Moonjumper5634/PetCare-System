package ph.gov.santarosa.petcare.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.Pet;
import ph.gov.santarosa.petcare.domain.ServiceRecord;
import ph.gov.santarosa.petcare.domain.ServiceRequest;
import ph.gov.santarosa.petcare.eligibility.ServiceNames;
import ph.gov.santarosa.petcare.repository.*;

/** Creates opt-in operational reminders. Daily de-duplication keeps restarts from sending duplicates. */
@Service
public class ScheduledNotificationService {
    private final SystemSettingRepository settings; private final ServiceRecordRepository records;
    private final ServiceRequestRepository requests; private final PetRepository pets; private final AppUserRepository users;
    private final NotificationRepository notifications; private final Notifier notifier; private final Clock clock;
    public ScheduledNotificationService(SystemSettingRepository settings,ServiceRecordRepository records,
            ServiceRequestRepository requests,PetRepository pets,AppUserRepository users,
            NotificationRepository notifications,Notifier notifier,@Qualifier("clock") Clock clock){
        this.settings=settings;this.records=records;this.requests=requests;this.pets=pets;this.users=users;
        this.notifications=notifications;this.notifier=notifier;this.clock=clock;
    }

    @Transactional
    @Scheduled(cron="${petcare.notifications.daily-cron:0 0 8 * * *}",zone="Asia/Manila")
    public void sendDailyReminders(){
        LocalDate today=LocalDate.now(clock);
        if(enabled("vaccination_reminders",true)) sendVaccinationReminders(today);
        if(today.getDayOfMonth()==1&&enabled("monthly_admin_summary",false)){
            String title="PetCARE monthly snapshot — "+today.getMonth()+" "+today.getYear();
            String body="Current totals: "+pets.count()+" dog records, "+requests.count()+" service requests, and "+users.countByRoleAndStatus(Values.OWNER,"Active")+" active resident accounts.";
            users.findByRole(Values.ADMIN).stream().filter(u->"Active".equals(u.getStatus())).forEach(u->notifyOnceToday(u.getId(),title,body,today));
        }
    }

    /** Only the newest completed anti-rabies record for each active dog may generate a reminder. */
    private void sendVaccinationReminders(LocalDate today){
        Map<String,ServiceRequest> requestById=new HashMap<>();
        requests.findAll().forEach(r->requestById.put(r.getId(),r));
        Map<String,ServiceRecord> latestByPet=new HashMap<>();
        Comparator<ServiceRecord> newest=Comparator.comparing(ServiceRecord::getRecordDate,
                Comparator.nullsFirst(Comparator.naturalOrder())).thenComparing(ServiceRecord::getId,
                Comparator.nullsFirst(Comparator.naturalOrder()));
        records.findAll().forEach(record->{
            ServiceRequest request=requestById.get(record.getRequestId());
            if(request==null||!ServiceNames.ANTI_RABIES.equals(request.getServiceName())) return;
            latestByPet.merge(request.getPetId(),record,(a,b)->newest.compare(a,b)>=0?a:b);
        });
        latestByPet.forEach((petId,record)->pets.findById(petId).filter(this::activePet).ifPresent(pet->{
            LocalDate due=record.getNextDue();
            if(due==null||due.isBefore(today)||due.isAfter(today.plusDays(30))) return;
            ServiceRequest request=requestById.get(record.getRequestId());
            String title="Vaccination reminder for "+pet.getName()+" — due "+due;
            notifyOnceInReminderWindow(request.getOwnerId(),title,
                    pet.getName()+"'s anti-rabies vaccination is due on "+due+". Please contact the CVO to arrange the next vaccination.",today);
        }));
    }

    private boolean activePet(Pet pet){return !Boolean.TRUE.equals(pet.getArchived());}
    private boolean enabled(String key,boolean fallback){return settings.findById(key).map(s->Boolean.parseBoolean(s.getValue())).orElse(fallback);}
    private void notifyOnceToday(String userId,String title,String body,LocalDate date){
        LocalDateTime from=date.atStartOfDay(),to=date.plusDays(1).atStartOfDay();
        if(!alreadyNotified(userId,title,from,to)) notifier.notify(userId,title,body);
    }
    private void notifyOnceInReminderWindow(String userId,String title,String body,LocalDate date){
        LocalDateTime from=date.minusDays(31).atStartOfDay(),to=date.plusDays(1).atStartOfDay();
        if(!alreadyNotified(userId,title,from,to)) notifier.notify(userId,title,body);
    }
    private boolean alreadyNotified(String userId,String title,LocalDateTime from,LocalDateTime to){
        return notifications.findByUserIdOrderByCreatedAtDesc(userId).stream().anyMatch(n->title.equals(n.getTitle())
                &&n.getCreatedAt()!=null&&!n.getCreatedAt().isBefore(from)&&n.getCreatedAt().isBefore(to));
    }
}
