package ph.gov.santarosa.petcare.service;

import ph.gov.santarosa.petcare.domain.AppUser;

/**
 * Delivers one-time codes (account verification, password reset).
 * TODO before go-live: implement this with the CVO's email or SMS provider and use it in the prod profile.
 */
public interface CodeSender {
    void send(AppUser user, String purpose, String code);
}
