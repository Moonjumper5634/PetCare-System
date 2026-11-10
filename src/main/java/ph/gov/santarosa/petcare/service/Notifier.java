package ph.gov.santarosa.petcare.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.domain.AuditLog;
import ph.gov.santarosa.petcare.domain.Notification;
import ph.gov.santarosa.petcare.repository.AuditLogRepository;
import ph.gov.santarosa.petcare.repository.NotificationRepository;

/** Writes the notification and audit rows that the front end used to push into its local arrays. */
@Service
public class Notifier {

    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");

    private final NotificationRepository notifications;
    private final AuditLogRepository audit;
    private final IdGenerator ids;
    private final Clock clock;

    public Notifier(NotificationRepository notifications, AuditLogRepository audit, IdGenerator ids,
                    @Qualifier("clock") Clock clock) {
        this.notifications = notifications;
        this.audit = audit;
        this.ids = ids;
        this.clock = clock;
    }

    /** Date from the application clock (fixed in dev), time of day from the real clock, so events keep their order. */
    public LocalDateTime now() { return LocalDateTime.of(LocalDate.now(clock), LocalTime.now(MANILA)); }

    public void notify(String userId, String title, String body) {
        Notification n = new Notification();
        n.setId(ids.next("notif"));
        n.setUserId(userId);
        n.setTitle(cut(title, 160));
        n.setBody(cut(body, 1000));
        n.setCreatedAt(now());
        n.setRead(Boolean.FALSE);
        notifications.save(n);
    }

    public void audit(AppUser actor, String action, String module, String refId, String description) {
        AuditLog a = new AuditLog();
        a.setId(ids.next("audit"));
        a.setOccurredAt(now());
        a.setUserId(actor.getId());
        a.setUserName(cut(actor.getFullName(), 160));
        a.setAction(cut(action, 40));
        a.setModule(cut(module, 60));
        a.setRefId(cut(refId, 40));
        a.setDescription(cut(description, 1000));
        audit.save(a);
    }

    @Transactional
    public void markRead(AppUser actor, String notificationId) {
        Notification n = notifications.findById(notificationId).orElse(null);
        // Someone else's notification looks exactly like a missing one.
        if (n == null || !n.getUserId().equals(actor.getId())) throw ApiException.notFound("Notification not found.");
        if (!Boolean.TRUE.equals(n.getRead())) { n.setRead(Boolean.TRUE); notifications.save(n); }
    }

    static String cut(String s, int max) { return s == null || s.length() <= max ? s : s.substring(0, max - 1) + "\u2026"; }
}
