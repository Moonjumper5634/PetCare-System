package ph.gov.santarosa.petcare.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ph.gov.santarosa.petcare.domain.AppUser;

/** Production delivery of short-lived sign-up and password-reset codes. */
@Profile("prod | email")
@Service
public class SmtpCodeSender implements CodeSender {
    private static final Logger LOG = Logger.getLogger(SmtpCodeSender.class.getName());
    private final JavaMailSender mail;
    private final String from;
    private final String officeName;

    public SmtpCodeSender(JavaMailSender mail,
                          @Value("${petcare.mail.from}") String from,
                          @Value("${petcare.mail.office-name}") String officeName) {
        this.mail = mail; this.from = from; this.officeName = officeName;
    }

    @Override
    public void send(AppUser user, String purpose, String code) {
        String action = "reset".equals(purpose) ? "reset your PetCARE password" : "verify your PetCARE account";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Your PetCARE verification code");
        message.setText("Hello " + user.getFirstName() + ",\n\nYour code to " + action + " is: " + code
                + "\n\nThis code expires in 15 minutes. If you did not request it, you can ignore this message."
                + "\n\n" + officeName);
        try {
            mail.send(message);
        } catch (MailException e) {
            LOG.log(Level.SEVERE, "PetCARE could not deliver a verification email through the configured SMTP service.", e);
            // Keep account creation available; the resident can sign in and resend verification later.
        }
    }
}
