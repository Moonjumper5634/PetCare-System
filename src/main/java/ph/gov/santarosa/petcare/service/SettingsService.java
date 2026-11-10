package ph.gov.santarosa.petcare.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.domain.SystemSetting;
import ph.gov.santarosa.petcare.repository.SystemSettingRepository;

@Service
public class SettingsService {
    public record Update(String officeName, String contactEmail, String contactNumber,
                         Boolean statusNotifications, Boolean vaccinationReminders, Boolean monthlyAdminSummary) { }
    private final SystemSettingRepository settings; private final Notifier notifier;
    public SettingsService(SystemSettingRepository settings, Notifier notifier) { this.settings=settings; this.notifier=notifier; }

    @Transactional(readOnly = true)
    public Map<String,Object> read(AppUser actor) {
        Access.require(actor, Values.ADMIN);
        Map<String,String> raw=new LinkedHashMap<>(); settings.findAll().forEach(s->raw.put(s.getKey(),s.getValue()));
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("officeName", raw.getOrDefault("office_name", "")); out.put("contactEmail", raw.getOrDefault("contact_email", ""));
        out.put("contactNumber", raw.getOrDefault("contact_number", ""));
        out.put("statusNotifications", Boolean.parseBoolean(raw.getOrDefault("status_notifications", "true")));
        out.put("vaccinationReminders", Boolean.parseBoolean(raw.getOrDefault("vaccination_reminders", "true")));
        out.put("monthlyAdminSummary", Boolean.parseBoolean(raw.getOrDefault("monthly_admin_summary", "false")));
        return out;
    }

    @Transactional
    public void update(AppUser actor, Update u) {
        Access.require(actor, Values.ADMIN);
        put("office_name", Validation.text(u.officeName(), "Office name", 160, true), actor);
        put("contact_email", Validation.email(u.contactEmail(), true), actor);
        put("contact_number", Validation.phone(u.contactNumber(), true), actor);
        put("status_notifications", String.valueOf(Boolean.TRUE.equals(u.statusNotifications())), actor);
        put("vaccination_reminders", String.valueOf(Boolean.TRUE.equals(u.vaccinationReminders())), actor);
        put("monthly_admin_summary", String.valueOf(Boolean.TRUE.equals(u.monthlyAdminSummary())), actor);
        notifier.audit(actor, "Update", "System Settings", "SYSTEM", "Organization and notification settings updated.");
    }
    private void put(String key,String value,AppUser actor){ SystemSetting s=settings.findById(key).orElseGet(SystemSetting::new); s.setKey(key); s.setValue(value); s.setUpdatedBy(actor.getId()); settings.save(s); }
}
