package ph.gov.santarosa.petcare.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.repository.AppUserRepository;

/**
 * Login, sign-up, verification, password reset and password change.
 *
 * Security rules implemented here:
 *  - passwords are bcrypt hashes; wrong email and wrong password give the same message and take similar time
 *  - 5 wrong passwords lock the account for 15 minutes
 *  - one-time codes: 6 digits, SecureRandom, expire after 15 minutes, 5 wrong tries, compared in constant time
 *  - the failure counters are saved even though an exception is thrown (noRollbackFor)
 */
@Service
public class AuthService {

    static final int MAX_LOGIN_FAILURES = 5;
    static final Duration LOCK_TIME = Duration.ofMinutes(15);
    static final int MAX_CODE_TRIES = 5;
    static final Duration CODE_LIFETIME = Duration.ofMinutes(15);
    private static final String INVALID_LOGIN = "Invalid email or password.";
    private static final SecureRandom RANDOM = new SecureRandom();

    /** What the web layer needs back from sign-up / resend / forgot. devCode is non-null only in the dev profile. */
    public record CodeIssued(String email, String devCode) { }

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final IdGenerator ids;
    private final Notifier notifier;
    private final CodeSender sender;
    private final Clock clock;
    private final boolean devReturnCodes;
    /** Hash of a throw-away password; checked when the email is unknown so timing does not reveal which emails exist. */
    private final String dummyHash;

    public AuthService(AppUserRepository users, PasswordEncoder encoder, IdGenerator ids, Notifier notifier,
                       CodeSender sender, @Qualifier("securityClock") Clock securityClock,
                       @Value("${petcare.dev-return-codes:false}") boolean devReturnCodes) {
        this.users = users;
        this.encoder = encoder;
        this.ids = ids;
        this.notifier = notifier;
        this.sender = sender;
        this.clock = securityClock;
        this.devReturnCodes = devReturnCodes;
        this.dummyHash = encoder.encode("not-a-real-password-" + RANDOM.nextInt());
    }

    /* ------------------------------ login ------------------------------ */

    @Transactional(noRollbackFor = ApiException.class)
    public AppUser login(String email, String password) {
        String e = email == null ? "" : email.trim().toLowerCase();
        String p = password == null ? "" : password;
        if (e.isEmpty() || e.length() > 190 || p.isEmpty() || p.length() > 128) throw ApiException.unauthorized(INVALID_LOGIN);

        AppUser u = users.findByEmailIgnoreCase(e).orElse(null);
        if (u == null) {
            encoder.matches(p, dummyHash);
            throw ApiException.unauthorized(INVALID_LOGIN);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (u.getLockedUntil() != null && u.getLockedUntil().isAfter(now)) {
            long mins = Math.max(1, Duration.between(now, u.getLockedUntil()).plusSeconds(59).toMinutes());
            throw new ApiException(429, "Too many failed attempts. Try again in " + mins + " minute" + (mins == 1 ? "" : "s") + ".");
        }

        if (!encoder.matches(p, u.getPasswordHash())) {
            int fails = u.getFailedLoginAttempts() == null ? 1 : u.getFailedLoginAttempts() + 1;
            if (fails >= MAX_LOGIN_FAILURES) {
                u.setFailedLoginAttempts(0);
                u.setLockedUntil(now.plus(LOCK_TIME));
                users.save(u);
                throw new ApiException(429, "Too many failed attempts. Try again in 15 minutes.");
            }
            u.setFailedLoginAttempts(fails);
            users.save(u);
            throw ApiException.unauthorized(INVALID_LOGIN);
        }

        // Correct password: only now say anything about the account's state.
        if (!"Active".equals(u.getStatus()))
            throw ApiException.forbidden("This account has been deactivated. Please contact the CVO office.");

        u.setFailedLoginAttempts(0);
        u.setLockedUntil(null);
        u.setLastLoginAt(now);
        users.save(u);
        return u;
    }

    /* ------------------------------ sign-up / verify ------------------------------ */

    public record SignupRequest(String firstName, String middleName, String lastName, String email, String mobile,
                                String houseStreet, String barangay, String city, String province,
                                String password, Boolean agree) { }

    @Transactional
    public CodeIssued signup(SignupRequest r) {
        String first = Validation.text(r.firstName(), "First name", 80, true);
        String last = Validation.text(r.lastName(), "Last name", 80, true);
        String middle = Validation.text(r.middleName(), "Middle name", 80, false);
        String email = Validation.email(r.email(), true);
        String mobile = Validation.phone(r.mobile(), true);
        String barangay = Validation.text(r.barangay(), "Barangay", 80, true);
        String street = Validation.text(r.houseStreet(), "House / street", 160, false);
        String city = Validation.text(r.city(), "City", 80, false);
        String province = Validation.text(r.province(), "Province", 80, false);
        Validation.password(r.password());
        if (!Boolean.TRUE.equals(r.agree())) throw ApiException.badRequest("You must agree to the terms to create an account.");
        if (users.findByEmailIgnoreCase(email).isPresent()) throw ApiException.conflict("An account with this email already exists");

        AppUser u = new AppUser();
        u.setId(ids.next("user"));
        u.setRole(Values.OWNER);
        u.setFirstName(first);
        u.setMiddleName(middle);
        u.setLastName(last);
        u.setFullName((first + " " + last).trim());
        u.setEmail(email);
        u.setMobile(mobile);
        u.setHouseStreet(street);
        u.setBarangay(barangay);
        u.setCity(city == null ? "Santa Rosa" : city);
        u.setProvince(province == null ? "Laguna" : province);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setStatus("Active");
        u.setInitials(initials(first, last));
        u.setMustChangePassword(Boolean.FALSE);
        u.setFailedLoginAttempts(0);
        u.setVerificationAttempts(0);
        String code = issueCode(u, "signup");
        users.save(u);
        notifier.audit(u, "Create", "User Management", u.getId(), "Resident account created \u2014 email verification pending.");
        sender.send(u, "signup", code);
        return new CodeIssued(email, devReturnCodes ? code : null);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void verify(String email, String code) {
        AppUser u = findByEmail(email);
        if (u == null || !"signup".equals(u.getVerificationPurpose())) throw ApiException.badRequest("Invalid or expired code.");
        checkCode(u, "signup", code);
        clearCode(u);
        users.save(u);
        notifier.audit(u, "Update", "User Management", u.getId(), "Resident email address verified.");
        notifier.notify(u.getId(), "Email verified successfully.", "Your PetCARE email address has been verified.");
    }

    /** Always answers the same way, whether or not the email exists. */
    @Transactional
    public CodeIssued resendSignupCode(String email) {
        AppUser u = findByEmail(email);
        String devCode = null;
        if (u != null && "Active".equals(u.getStatus()) && "signup".equals(u.getVerificationPurpose())) {
            String code = issueCode(u, "signup");
            users.save(u);
            sender.send(u, "signup", code);
            if (devReturnCodes) devCode = code;
        }
        return new CodeIssued(clean(email), devCode);
    }

    /** Issues the same email-ownership code for administrator-provisioned staff/admin or walk-in accounts. */
    @Transactional
    public void sendInitialEmailVerification(AppUser u) {
        String code = issueCode(u, "signup");
        users.save(u);
        sender.send(u, "signup", code);
    }

    /* ------------------------------ forgot / reset ------------------------------ */

    /** Always answers the same way, so it cannot be used to find out which emails have accounts. */
    @Transactional
    public CodeIssued forgotPassword(String email) {
        AppUser u = findByEmail(email);
        String devCode = null;
        if (u != null && "Active".equals(u.getStatus())) {
            String code = issueCode(u, "reset");
            users.save(u);
            sender.send(u, "reset", code);
            if (devReturnCodes) devCode = code;
        }
        return new CodeIssued(clean(email), devCode);
    }

    /** Step 2 of the reset screen: is the code right? (It is checked again, with the new password, in {@link #resetPassword}.) */
    @Transactional(noRollbackFor = ApiException.class)
    public void checkResetCode(String email, String code) {
        AppUser u = findByEmail(email);
        if (u == null) throw ApiException.badRequest("Invalid or expired code.");
        checkCode(u, "reset", code);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void resetPassword(String email, String code, String newPassword) {
        AppUser u = findByEmail(email);
        if (u == null || !"Active".equals(u.getStatus())) throw ApiException.badRequest("Invalid or expired code.");
        checkCode(u, "reset", code);
        Validation.password(newPassword);
        u.setPasswordHash(encoder.encode(newPassword));
        u.setMustChangePassword(Boolean.FALSE);
        u.setFailedLoginAttempts(0);
        u.setLockedUntil(null);
        clearCode(u);
        users.save(u);
        notifier.audit(u, "Update", "User Management", u.getId(), "Password reset via Forgot Password flow.");
    }

    /* ------------------------------ change password (signed in) ------------------------------ */

    @Transactional
    public void changePassword(AppUser actor, String currentPassword, String newPassword) {
        AppUser u = users.findById(actor.getId()).orElseThrow(() -> ApiException.unauthorized("Please sign in."));
        if (currentPassword == null || !encoder.matches(currentPassword, u.getPasswordHash()))
            throw ApiException.badRequest("Current password is incorrect.");
        Validation.password(newPassword);
        if (newPassword.equals(currentPassword)) throw ApiException.badRequest("New password must be different from the current one.");
        u.setPasswordHash(encoder.encode(newPassword));
        u.setMustChangePassword(Boolean.FALSE);
        users.save(u);
        notifier.audit(u, "Update", "User Management", u.getId(), "Password changed from Profile.");
    }

    /* ------------------------------ helpers ------------------------------ */

    private AppUser findByEmail(String email) {
        String e = clean(email);
        return e.isEmpty() ? null : users.findByEmailIgnoreCase(e).orElse(null);
    }

    private static String clean(String email) { return email == null ? "" : email.trim().toLowerCase(); }

    private String issueCode(AppUser u, String purpose) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        u.setVerificationCode(code);
        u.setVerificationPurpose(purpose);
        u.setVerificationExpiresAt(LocalDateTime.now(clock).plus(CODE_LIFETIME));
        u.setVerificationAttempts(0);
        return code;
    }

    private static void clearCode(AppUser u) {
        u.setVerificationCode(null);
        u.setVerificationPurpose(null);
        u.setVerificationExpiresAt(null);
        u.setVerificationAttempts(0);
    }

    private void checkCode(AppUser u, String purpose, String supplied) {
        if (u.getVerificationCode() == null || !purpose.equals(u.getVerificationPurpose()))
            throw ApiException.badRequest("Invalid or expired code.");
        if (u.getVerificationAttempts() != null && u.getVerificationAttempts() >= MAX_CODE_TRIES)
            throw new ApiException(429, "Too many incorrect attempts. Please request a new code.");
        LocalDateTime now = LocalDateTime.now(clock);
        if (u.getVerificationExpiresAt() == null || !now.isBefore(u.getVerificationExpiresAt()))
            throw ApiException.badRequest("Invalid or expired code.");
        byte[] a = (supplied == null ? "" : supplied.trim()).getBytes(StandardCharsets.UTF_8);
        byte[] b = u.getVerificationCode().getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(a, b)) {
            u.setVerificationAttempts((u.getVerificationAttempts() == null ? 0 : u.getVerificationAttempts()) + 1);
            users.save(u);
            throw ApiException.badRequest("Invalid verification code.");
        }
    }

    public static String initials(String first, String last) {
        String f = first == null || first.isEmpty() ? "" : first.substring(0, 1);
        String l = last == null || last.isEmpty() ? "" : last.substring(0, 1);
        return (f + l).toUpperCase();
    }
}
