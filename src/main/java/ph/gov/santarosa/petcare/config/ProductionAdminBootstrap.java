package ph.gov.santarosa.petcare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.repository.AppUserRepository;
import ph.gov.santarosa.petcare.service.AuthService;
import ph.gov.santarosa.petcare.service.IdGenerator;
import ph.gov.santarosa.petcare.service.Validation;

/** Creates the first production administrator once. Later administrators use User Management. */
@Profile("prod")
@Component
public class ProductionAdminBootstrap implements ApplicationRunner {
    private final AppUserRepository users; private final PasswordEncoder encoder; private final IdGenerator ids;
    private final String email; private final String password; private final String name;

    public ProductionAdminBootstrap(AppUserRepository users, PasswordEncoder encoder, IdGenerator ids,
            @Value("${PETCARE_BOOTSTRAP_ADMIN_EMAIL:}") String email,
            @Value("${PETCARE_BOOTSTRAP_ADMIN_PASSWORD:}") String password,
            @Value("${PETCARE_BOOTSTRAP_ADMIN_NAME:System Administrator}") String name) {
        this.users=users;this.encoder=encoder;this.ids=ids;this.email=email;this.password=password;this.name=name;
    }

    @Override @Transactional
    public void run(ApplicationArguments args) {
        if (!users.findByRole("admin").isEmpty()) return;
        String cleanEmail=Validation.email(email,true); Validation.password(password);
        String cleanName=Validation.text(name,"Administrator name",160,true);
        String[] parts=cleanName.split("\\s+",2);
        AppUser u=new AppUser(); u.setId(ids.next("user")); u.setRole("admin"); u.setFirstName(parts[0]);
        u.setLastName(parts.length>1?parts[1]:null); u.setFullName(cleanName); u.setEmail(cleanEmail);
        u.setCity("Santa Rosa");u.setProvince("Laguna");u.setPasswordHash(encoder.encode(password));u.setStatus("Active");
        u.setInitials(AuthService.initials(parts[0],parts.length>1?parts[1]:""));u.setJobTitle("System Administrator");
        u.setMustChangePassword(Boolean.TRUE);u.setFailedLoginAttempts(0);u.setVerificationAttempts(0);users.save(u);
    }
}
