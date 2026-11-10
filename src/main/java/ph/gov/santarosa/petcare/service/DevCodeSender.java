package ph.gov.santarosa.petcare.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.santarosa.petcare.domain.AppUser;

/** Explicit local-demo delivery. The API may return the OTP, but it is never written to server logs. */
@Profile("mock-otp")
@Service
public class DevCodeSender implements CodeSender {
    @Override
    public void send(AppUser user, String purpose, String code) {
        // Intentionally empty: AuthService returns the code only when mock-otp enables dev-return-codes.
    }
}
