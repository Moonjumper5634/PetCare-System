package ph.gov.santarosa.petcare.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.repository.AppUserRepository;
import ph.gov.santarosa.petcare.service.ApiException;

/**
 * The signed-in user, re-read from the database on every call. The session only stores the user id, so
 * deactivating an account (or changing a role) takes effect on that user's very next request.
 */
@Component
public class CurrentUser {

    private final AppUserRepository users;

    public CurrentUser(AppUserRepository users) { this.users = users; }

    public AppUser require() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken)
            throw ApiException.unauthorized("Please sign in.");
        AppUser u = users.findById(String.valueOf(a.getPrincipal())).orElseThrow(() -> ApiException.unauthorized("Please sign in."));
        if (!"Active".equals(u.getStatus())) throw ApiException.unauthorized("Your session has ended. Please sign in again.");
        return u;
    }

    /** Like require(), but refuses while a temporary password is still in use. */
    public AppUser requireReady() {
        AppUser u = require();
        if (Boolean.TRUE.equals(u.getMustChangePassword()))
            throw new ApiException(403, "PASSWORD_CHANGE_REQUIRED", "Please change your temporary password before continuing.");
        return u;
    }
}
