package ph.gov.santarosa.petcare.service;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ph.gov.santarosa.petcare.domain.AppUser;

/** Railway-safe delivery through Brevo's HTTPS transactional-email API. */
@Profile("prod | email")
@ConditionalOnProperty(name = "petcare.mail.provider", havingValue = "brevo-api")
@Service
public class BrevoApiCodeSender implements CodeSender {
    private static final Logger LOG = Logger.getLogger(BrevoApiCodeSender.class.getName());
    private final RestClient client;
    private final String from;
    private final String officeName;

    public BrevoApiCodeSender(RestClient.Builder builder,
                              @Value("${petcare.mail.brevo-api-key}") String apiKey,
                              @Value("${petcare.mail.from}") String from,
                              @Value("${petcare.mail.office-name}") String officeName) {
        this.client = builder.baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .build();
        this.from = from;
        this.officeName = officeName;
    }

    @Override
    public void send(AppUser user, String purpose, String code) {
        String action = "reset".equals(purpose) ? "reset your PetCARE password" : "verify your PetCARE account";
        String text = "Hello " + user.getFirstName() + ",\n\nYour code to " + action + " is: " + code
                + "\n\nThis code expires in 15 minutes. If you did not request it, you can ignore this message."
                + "\n\n" + officeName;
        Map<String, Object> request = Map.of(
                "sender", Map.of("name", officeName, "email", from),
                "to", List.of(Map.of("email", user.getEmail(), "name", user.getFirstName())),
                "subject", "Your PetCARE verification code",
                "textContent", text);
        try {
            client.post().uri("/smtp/email").body(request).retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            LOG.log(Level.SEVERE, "PetCARE could not deliver a verification email through Brevo HTTPS.", e);
            // Preserve account access; the resident can request a fresh code after delivery recovers.
        }
    }
}
