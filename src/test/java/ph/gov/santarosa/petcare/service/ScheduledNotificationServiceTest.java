package ph.gov.santarosa.petcare.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import ph.gov.santarosa.petcare.domain.ServiceRecord;
import ph.gov.santarosa.petcare.domain.ServiceRequest;
import ph.gov.santarosa.petcare.eligibility.ServiceNames;
import ph.gov.santarosa.petcare.support.Kit;

class ScheduledNotificationServiceTest {
    private final Kit k=new Kit();

    @Test
    void onlyLatestAntiRabiesRecordForActivePetCreatesOneReminder(){
        request("REQ-OLD",ServiceNames.ANTI_RABIES);
        record("REQ-OLD",Kit.TODAY.minusYears(1),Kit.TODAY.plusDays(5));
        request("REQ-NEW",ServiceNames.ANTI_RABIES);
        ServiceRecord newest=record("REQ-NEW",Kit.TODAY.minusDays(10),Kit.TODAY.plusDays(60));
        request("REQ-CHIP","Microchipping");
        record("REQ-CHIP",Kit.TODAY.minusDays(5),Kit.TODAY.plusDays(3));

        k.scheduledNotifications.sendDailyReminders();
        assertEquals(0,k.notifications.findAll().size(),"An older vaccination and a different service must not remind");

        newest.setNextDue(Kit.TODAY.plusDays(10));
        k.records.save(newest);
        k.scheduledNotifications.sendDailyReminders();
        k.scheduledNotifications.sendDailyReminders();
        assertEquals(1,k.notifications.findAll().size(),"The same due-date reminder is de-duplicated");

        k.buddy.setArchived(true);
        newest.setNextDue(Kit.TODAY.plusDays(11));
        k.records.save(newest);
        k.scheduledNotifications.sendDailyReminders();
        assertEquals(1,k.notifications.findAll().size(),"Archived dogs do not receive reminders");
    }

    private void request(String id,String service){
        ServiceRequest r=new ServiceRequest();
        r.setId(id);r.setPetId(k.buddy.getId());r.setOwnerId(k.maria.getId());r.setServiceName(service);
        r.setSubmittedDate(LocalDate.now(k.appClock));r.setSource("Online");r.setStaffDecision("Approved");r.setStatus("Completed");
        k.requests.save(r);
    }

    private ServiceRecord record(String requestId,LocalDate date,LocalDate due){
        ServiceRecord r=new ServiceRecord();r.setRequestId(requestId);r.setRecordDate(date);r.setNextDue(due);
        r.setStaffName(k.rowena.getFullName());r.setSourceType("Request");r.setServiceLocation("CVO");
        return k.records.save(r);
    }
}
