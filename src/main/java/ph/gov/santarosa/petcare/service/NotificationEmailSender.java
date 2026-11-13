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

    public void send(String userId, String title, String body) {
        if (!enabled) return;
        users.findById(userId).filter(u -> u.getEmail() != null && !u.getEmail().endsWith(".invalid")).ifPresent(u -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from); message.setTo(u.getEmail());
            message.setSubject("DogCARE: " + title);
            message.setText("Hello " + u.getFirstName() + ",\n\n" + body + "\n\nYou can view the full update in DogCARE.\n\n" + officeName);
            try { mail.send(message); }
            catch (MailException e) { LOG.log(Level.WARNING, "Could not send notification email; the in-app notification was saved.", e); }
        });
    }
}
