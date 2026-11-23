package ph.gov.santarosa.petcare.service;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ph.gov.santarosa.petcare.repository.AppUserRepository;

/** Optional email copy of an in-app notification. The database notification remains authoritative. */
@Service
public class NotificationEmailSender {
    public record DeliveryResult(boolean attempted, boolean delivered, String error) { }

    private static final Logger LOG = Logger.getLogger(NotificationEmailSender.class.getName());
    private final JavaMailSender mail;
    private final RestClient brevo;
    private final AppUserRepository users;
    private final boolean enabled;
    private final String provider;
    private final String from;
    private final String officeName;

    public NotificationEmailSender(ObjectProvider<JavaMailSender> mailProvider, AppUserRepository users,
            RestClient.Builder restClientBuilder,
            @Value("${petcare.mail.notifications-enabled:false}") boolean enabled,
            @Value("${petcare.mail.provider:smtp}") String provider,
            @Value("${petcare.mail.brevo-api-key:}") String brevoApiKey,
            @Value("${petcare.mail.from:}") String from,
            @Value("${petcare.mail.office-name:Santa Rosa City Veterinary Office}") String officeName) {
        this.mail = mailProvider.getIfAvailable();
        this.users = users;
        this.provider = provider == null ? "smtp" : provider.trim().toLowerCase();
        this.brevo = "brevo-api".equals(this.provider) && brevoApiKey != null && !brevoApiKey.isBlank()
                ? restClientBuilder.baseUrl("https://api.brevo.com/v3").defaultHeader("api-key", brevoApiKey).build()
                : null;
        this.enabled = enabled && (("smtp".equals(this.provider) && this.mail != null)
                || ("brevo-api".equals(this.provider) && this.brevo != null));
        this.from = from;
        this.officeName = officeName;
    }

    public boolean isEnabled() { return enabled; }

    public DeliveryResult send(String userId, String title, String body) {
        if (!enabled) return new DeliveryResult(false, false, "Email notifications are disabled.");
        var user = users.findById(userId).orElse(null);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank() || user.getEmail().endsWith(".invalid"))
            return new DeliveryResult(false, false, "The account does not have a deliverable email address.");
        String subject = "PetCARE: " + title;
        String text = "Hello " + user.getFirstName() + ",\n\n" + body
                + "\n\nYou can view the full update in PetCARE.\n\n" + officeName;
        try {
            if ("brevo-api".equals(provider)) {
                Map<String, Object> request = Map.of(
                        "sender", Map.of("name", officeName, "email", from),
                        "to", List.of(Map.of("email", user.getEmail(), "name", user.getFirstName())),
                        "subject", subject,
                        "textContent", text);
                brevo.post().uri("/smtp/email").body(request).retrieve().toBodilessEntity();
            } else {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from); message.setTo(user.getEmail()); message.setSubject(subject); message.setText(text);
                mail.send(message);
            }
            return new DeliveryResult(true, true, null);
        } catch (MailException | RestClientException e) {
            LOG.log(Level.WARNING, "Could not send notification email through " + provider
                    + "; the in-app notification was saved for retry.", e);
            String error = e instanceof MailException mailError && mailError.getMostSpecificCause() != null
                    ? mailError.getMostSpecificCause().getMessage() : e.getMessage();
            return new DeliveryResult(true, false, error);
        }
    }
}
