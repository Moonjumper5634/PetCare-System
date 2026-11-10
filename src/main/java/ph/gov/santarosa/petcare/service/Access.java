package ph.gov.santarosa.petcare.service;

import java.util.Arrays;
import ph.gov.santarosa.petcare.domain.AppUser;

/** Role checks used by every workflow method. The role comes from the database row, never from the browser. */
public final class Access {
    private Access() { }

    public static void require(AppUser actor, String... roles) {
        if (actor == null || !Arrays.asList(roles).contains(actor.getRole()))
            throw ApiException.forbidden("You do not have permission to do that.");
    }
}
