package ph.gov.santarosa.petcare.service;

import ph.gov.santarosa.petcare.domain.AppUser;

/** Delivers one-time codes for account verification and password reset. */
public interface CodeSender {
    /** Returns true only after the configured provider accepts the message. */
    boolean send(AppUser user, String purpose, String code);
}
