package ph.gov.santarosa.petcare.service;

import java.util.logging.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.santarosa.petcare.domain.AppUser;

/** Safe fallback: never exposes an OTP; residents can still create an account and verify later. */
@Profile("!prod & !email & !mock-otp")
@Service
public class UnconfiguredCodeSender implements CodeSender {
    private static final Logger LOG = Logger.getLogger(UnconfiguredCodeSender.class.getName());

    @Override
    public void send(AppUser user, String purpose, String code) {
        LOG.severe("Email verification is not configured; no code was delivered for user " + user.getId() + ".");
    }
}
