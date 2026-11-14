package ph.gov.santarosa.petcare.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ph.gov.santarosa.petcare.repository.AppUserRepository;

/** Optional email copy of an in-app notification. The database notification remains authoritative. */
@Service
public class NotificationEmailSender {
    public record DeliveryResult(boolean attempted, boolean delivered, String error) { }

    private static final Logger LOG = Logger.getLogger(NotificationEmailSender.class.getName());
    private final JavaMailSender mail;
    private final AppUserRepository users;
    private final boolean enabled;
    private final String from;
    private final String officeName;

    public NotificationEmailSender(ObjectProvider<JavaMailSender> mailProvider, AppUserRepository users,
            @Value("${petcare.mail.notifications-enabled:false}") boolean enabled,
            @Value("${petcare.mail.from:}") String from,
            @Value("${petcare.mail.office-name:Santa Rosa City Veterinary Office}") String officeName) {
        this.mail = mailProvider.getIfAvailable();
        this.users = users;
        this.enabled = enabled && this.mail != null;
        this.from = from;
        this.officeName = officeName;
    }

    public boolean isEnabled() { return enabled; }

    public DeliveryResult send(String userId, String title, String body) {
        if (!enabled) return new DeliveryResult(false, false, "Email notifications are disabled.");
        var user = users.findById(userId).orElse(null);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank() || user.getEmail().endsWith(".invalid"))
            return new DeliveryResult(false, false, "The account does not have a deliverable email address.");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("PetCARE: " + title);
        message.setText("Hello " + user.getFirstName() + ",\n\n" + body
                + "\n\nYou can view the full update in PetCARE.\n\n" + officeName);
        try {
            mail.send(message);
            return new DeliveryResult(true, true, null);
        } catch (MailException e) {
            LOG.log(Level.WARNING, "Could not send notification email; the in-app notification was saved for retry.", e);
            return new DeliveryResult(true, false, e.getMostSpecificCause().getMessage());
        }
    }
}
