package ph.gov.santarosa.petcare.service;

import java.security.SecureRandom;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.domain.EligibilityCriterion;
import ph.gov.santarosa.petcare.repository.AppUserRepository;
import ph.gov.santarosa.petcare.repository.EligibilityCriterionRepository;

/** Walk-in resident accounts (staff), user activation and eligibility criteria (administrator). */
@Service
public class AdminService {

    public record CreateResident(String name, String mobile, String email, String address, String barangay) { }
    /** The temporary password is returned once, for the staff member to hand to the resident. It is never stored in clear. */
    public record ResidentCreated(String userId, String email, String temporaryPassword) { }
    public record CreatePersonnel(String name, String email, String mobile, String role, String jobTitle) { }
    public record PersonnelCreated(String userId, String email, String temporaryPassword, String role) { }
    public record CriterionUpdate(Boolean enabled, String label, String description) {
        public CriterionUpdate(Boolean enabled, String description) { this(enabled, null, description); }
    }
    public record NewCriterion(String service, String label, String description) { }

    private static final String PW_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AppUserRepository users;
    private final EligibilityCriterionRepository criteria;
    private final PasswordEncoder encoder;
    private final IdGenerator ids;
    private final Notifier notifier;
    private final AuthService auth;

    public AdminService(AppUserRepository users, EligibilityCriterionRepository criteria, PasswordEncoder encoder,
                        IdGenerator ids, Notifier notifier, AuthService auth) {
        this.users = users; this.criteria = criteria; this.encoder = encoder; this.ids = ids; this.notifier = notifier;
        this.auth = auth;
    }

    @Transactional
    public ResidentCreated createResident(AppUser actor, CreateResident r) {
        Access.require(actor, Values.STAFF);
        String name = Validation.text(r.name(), "Resident name", 160, true);
        String mobile = Validation.phone(r.mobile(), false);
        String email = Validation.email(r.email(), false);
        String address = Validation.text(r.address(), "Address", 160, false);
        String barangay = Validation.text(r.barangay(), "Barangay", 80, true);
        String[] parts = name.split("\\s+", 2);

        String id = ids.next("user");
        // No email given: a placeholder on a reserved ".invalid" domain (can never be a real mailbox).
        if (email == null) email = "walk-in-" + id.toLowerCase() + "@walk-in.invalid";
        if (users.findByEmailIgnoreCase(email).isPresent()) throw ApiException.conflict("An account with this email already exists.");

        String temp = temporaryPassword();
        AppUser u = new AppUser();
        u.setId(id);
        u.setRole(Values.OWNER);
        u.setFirstName(parts[0]);
        u.setLastName(parts.length > 1 ? parts[1] : null);
        u.setFullName(name);
        u.setEmail(email);
        u.setMobile(mobile);
        u.setHouseStreet(address);
        u.setBarangay(barangay);
        u.setCity("Santa Rosa");
        u.setProvince("Laguna");
        u.setPasswordHash(encoder.encode(temp));
        u.setStatus("Active");
        u.setInitials(AuthService.initials(parts[0], parts.length > 1 ? parts[1] : ""));
        u.setMustChangePassword(Boolean.TRUE);
        u.setFailedLoginAttempts(0);
        u.setVerificationAttempts(0);
        users.save(u);
        if (!email.endsWith("@walk-in.invalid")) auth.sendInitialEmailVerification(u);
        notifier.audit(actor, "Create", "User Management", id, "Resident account created for " + name + " via Walk-in.");
        return new ResidentCreated(id, email, temp);
    }

    /** Administrator-only provisioning. Public sign-up can never create privileged accounts. */
    @Transactional
    public PersonnelCreated createPersonnel(AppUser actor, CreatePersonnel r) {
        Access.require(actor, Values.ADMIN);
        String name = Validation.text(r.name(), "Personnel name", 160, true);
        String email = Validation.email(r.email(), true);
        String mobile = Validation.phone(r.mobile(), false);
        String role = Validation.text(r.role(), "Role", 10, true);
        if (!Values.STAFF.equals(role) && !Values.ADMIN.equals(role))
            throw ApiException.badRequest("Role must be staff or admin.");
        String jobTitle = Validation.text(r.jobTitle(), "Job title", 80, true);
        if (users.findByEmailIgnoreCase(email).isPresent())
            throw ApiException.conflict("An account with this email already exists.");

        String[] parts = name.split("\\s+", 2);
        String id = ids.next("user");
        String temp = temporaryPassword();
        AppUser u = new AppUser();
        u.setId(id); u.setRole(role); u.setFirstName(parts[0]);
        u.setLastName(parts.length > 1 ? parts[1] : null); u.setFullName(name);
        u.setEmail(email); u.setMobile(mobile); u.setJobTitle(jobTitle);
        u.setCity("Santa Rosa"); u.setProvince("Laguna");
        u.setPasswordHash(encoder.encode(temp)); u.setStatus("Active");
        u.setInitials(AuthService.initials(parts[0], parts.length > 1 ? parts[1] : ""));
        u.setMustChangePassword(Boolean.TRUE); u.setFailedLoginAttempts(0); u.setVerificationAttempts(0);
        users.save(u);
        auth.sendInitialEmailVerification(u);
        notifier.audit(actor, "Create", "User Management", id,
                (Values.ADMIN.equals(role) ? "Administrator" : "CVO staff") + " account created for " + name + ".");
        return new PersonnelCreated(id, email, temp, role);
    }

    @Transactional
    public void setUserStatus(AppUser actor, String userId, String status) {
        Access.require(actor, Values.ADMIN);
        if (!"Active".equals(status) && !"Inactive".equals(status)) throw ApiException.badRequest("Status must be Active or Inactive.");
        AppUser u = users.findById(userId == null ? "" : userId).orElseThrow(() -> ApiException.notFound("User not found."));
        if (!"Active".equals(u.getStatus()) && !"Inactive".equals(u.getStatus()))
            throw ApiException.conflict("Only active or inactive accounts can be changed.");
        if (u.getId().equals(actor.getId()) && "Inactive".equals(status)) throw ApiException.badRequest("You cannot deactivate your own account.");
        if (Values.ADMIN.equals(u.getRole()) && "Inactive".equals(status) && "Active".equals(u.getStatus())
                && users.countByRoleAndStatus(Values.ADMIN, "Active") <= 1)
            throw ApiException.conflict("At least one active administrator is required.");
        if (status.equals(u.getStatus())) return;
        u.setStatus(status);
        users.save(u);
        notifier.audit(actor, "Update", "User Management", u.getId(), "User " + u.getFullName() + " set to " + status + ".");
    }

    @Transactional
    public void addCriterion(AppUser actor, NewCriterion r) {
        Access.require(actor, Values.ADMIN);
        String service = Validation.text(r.service(), "Service", 40, true);
        String label = Validation.text(r.label(), "Requirement label", 120, true);
        String description = Validation.text(r.description(), "Requirement description", 600, true);
        var rows = criteria.findByServiceNameOrderBySortOrder(service);
        if (rows.isEmpty()) throw ApiException.badRequest("Unknown veterinary service.");
        EligibilityCriterion c = new EligibilityCriterion();
        c.setServiceName(service);
        c.setCriterionKey("custom-" + System.currentTimeMillis());
        c.setLabel(label); c.setDescription(description); c.setEnabled(Boolean.TRUE);
        c.setCustomRule(Boolean.TRUE);
        c.setSortOrder(rows.stream().mapToInt(EligibilityCriterion::getSortOrder).max().orElse(0) + 10);
        criteria.save(c);
        notifier.audit(actor, "Create", "Eligibility Criteria", service, "Manual-review requirement added: " + label + ".");
    }

    @Transactional
    public void updateCriterion(AppUser actor, String service, String key, CriterionUpdate up) {
        Access.require(actor, Values.ADMIN);
        EligibilityCriterion c = criteria.findByServiceNameAndCriterionKey(service == null ? "" : service, key == null ? "" : key)
                .orElseThrow(() -> ApiException.notFound("Criterion not found."));
        if (up.enabled() == null && up.label() == null && up.description() == null) throw ApiException.badRequest("Nothing to update.");
        if (up.label() != null) c.setLabel(Validation.text(up.label(), "Requirement label", 120, true));
        if (up.description() != null) {
            c.setDescription(Validation.text(up.description(), "Description", 600, true));
            notifier.audit(actor, "Update", "Eligibility Criteria", service, "Updated criterion \"" + c.getLabel() + "\" for " + service + ".");
        }
        if (up.enabled() != null && !up.enabled().equals(c.getEnabled())) {
            c.setEnabled(up.enabled());
            notifier.audit(actor, "Update", "Eligibility Criteria", service,
                    "Criterion \"" + c.getLabel() + "\" for " + service + " set to " + (up.enabled() ? "Active" : "Disabled") + ".");
        }
        criteria.save(c);
    }

    private static String temporaryPassword() {
        while (true) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) sb.append(PW_ALPHABET.charAt(RANDOM.nextInt(PW_ALPHABET.length())));
            String pw = sb.toString();
            try { Validation.password(pw); return pw; } catch (ApiException e) { /* needs a digit and a letter: draw again */ }
        }
    }
}
