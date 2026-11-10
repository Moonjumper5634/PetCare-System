package ph.gov.santarosa.petcare.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.support.Kit;

class AuthServiceTest {

    Kit k;

    @BeforeEach
    void setUp() { k = new Kit(); }

    private ApiException fails(Executable a) { return assertThrows(ApiException.class, a); }

    private AuthService.SignupRequest signup(String email, String password) {
        return new AuthService.SignupRequest("Ana", null, "Reyes", email, "09171234567", "1 Mabini St", "Pooc", null, null, password, true);
    }

    /* ---- login ---- */

    @Test
    void correctPasswordLogsIn() {
        AppUser u = k.authFlow.login("  MARIA@test.ph ", "Resident#2026");     // email is trimmed and case-insensitive
        assertEquals("U-1001", u.getId());
        assertNotNull(u.getLastLoginAt());
    }

    @Test
    void unknownEmailAndWrongPasswordGiveTheSameMessage() {
        ApiException a = fails(() -> k.authFlow.login("nobody@test.ph", "Whatever1"));
        ApiException b = fails(() -> k.authFlow.login("maria@test.ph", "Wrong-password1"));
        assertEquals(401, a.getStatus());
        assertEquals(401, b.getStatus());
        assertEquals(a.getMessage(), b.getMessage());
    }

    @Test
    void fiveWrongPasswordsLockTheAccountEvenForTheRightPassword() {
        for (int i = 0; i < 4; i++) assertEquals(401, fails(() -> k.authFlow.login("maria@test.ph", "bad" + 1)).getStatus());
        assertEquals(429, fails(() -> k.authFlow.login("maria@test.ph", "bad")).getStatus());          // 5th failure locks
        assertEquals(429, fails(() -> k.authFlow.login("maria@test.ph", "Resident#2026")).getStatus()); // right password, still locked
        k.securityClock.advanceMinutes(16);
        assertEquals("U-1001", k.authFlow.login("maria@test.ph", "Resident#2026").getId());           // lock expired
        assertEquals(0, k.users.findById("U-1001").orElseThrow().getFailedLoginAttempts());
    }

    @Test
    void aSuccessfulLoginResetsTheFailureCount() {
        for (int i = 0; i < 3; i++) fails(() -> k.authFlow.login("maria@test.ph", "bad"));
        k.authFlow.login("maria@test.ph", "Resident#2026");
        for (int i = 0; i < 4; i++) assertEquals(401, fails(() -> k.authFlow.login("maria@test.ph", "bad")).getStatus());   // not locked yet
    }

    @Test
    void accountStateIsOnlyRevealedAfterTheCorrectPassword() {
        k.maria.setStatus("Inactive");
        assertEquals(401, fails(() -> k.authFlow.login("maria@test.ph", "wrong-Pass1")).getStatus());
        assertEquals(403, fails(() -> k.authFlow.login("maria@test.ph", "Resident#2026")).getStatus());
        k.maria.setStatus("Pending Verification");
        assertEquals(403, fails(() -> k.authFlow.login("maria@test.ph", "Resident#2026")).getStatus());
    }

    @Test
    void absurdInputIsRefusedWithoutHashing() {
        assertEquals(401, fails(() -> k.authFlow.login("", "x")).getStatus());
        assertEquals(401, fails(() -> k.authFlow.login("maria@test.ph", "x".repeat(500))).getStatus());
        assertEquals(401, fails(() -> k.authFlow.login(null, null)).getStatus());
    }

    /* ---- sign-up and verification ---- */

    @Test
    void signupVerifyThenLogin() {
        AuthService.CodeIssued c = k.authFlow.signup(signup("ana@test.ph", "Secret123"));
        assertNotNull(c.devCode());                                         // dev profile returns the code
        assertEquals(c.devCode(), k.sender.lastCode);
        AppUser beforeVerification = k.authFlow.login("ana@test.ph", "Secret123");
        assertEquals("signup", beforeVerification.getVerificationPurpose()); // accessible, but email is visibly unverified
        k.authFlow.verify("ana@test.ph", c.devCode());
        AppUser u = k.authFlow.login("ana@test.ph", "Secret123");
        assertEquals("owner", u.getRole());
        assertEquals("Active", u.getStatus());
        assertNull(u.getVerificationCode());                                // the code is single-use
        assertFalse(k.notifications.findByUserIdOrderByCreatedAtDesc(u.getId()).isEmpty());
    }

    @Test
    void signupAlwaysCreatesAResident() {
        k.authFlow.signup(signup("ana@test.ph", "Secret123"));
        assertEquals("owner", k.users.findByEmailIgnoreCase("ana@test.ph").orElseThrow().getRole());
    }

    @Test
    void signupRejectsBadInput() {
        assertEquals(409, fails(() -> k.authFlow.signup(signup("MARIA@test.ph", "Secret123"))).getStatus());   // duplicate, any case
        assertEquals(400, fails(() -> k.authFlow.signup(signup("not-an-email", "Secret123"))).getStatus());
        assertEquals(400, fails(() -> k.authFlow.signup(signup("a@test.ph", "short1"))).getStatus());
        assertEquals(400, fails(() -> k.authFlow.signup(signup("a@test.ph", "onlyletters"))).getStatus());
        assertEquals(400, fails(() -> k.authFlow.signup(signup("a@test.ph", "12345678"))).getStatus());
        assertEquals(400, fails(() -> k.authFlow.signup(new AuthService.SignupRequest("Ana", null, "Reyes", "a@test.ph", "0917", null, "Pooc", null, null, "Secret123", false))).getStatus());
    }

    @Test
    void wrongCodesAreLimitedThenBlockEvenTheRightCode() {
        String code = k.authFlow.signup(signup("ana@test.ph", "Secret123")).devCode();
        String wrong = code.equals("000000") ? "111111" : "000000";
        for (int i = 0; i < 5; i++) assertEquals(400, fails(() -> k.authFlow.verify("ana@test.ph", wrong)).getStatus());
        assertEquals(429, fails(() -> k.authFlow.verify("ana@test.ph", code)).getStatus());
        String fresh = k.authFlow.resendSignupCode("ana@test.ph").devCode();      // a new code resets the counter
        k.authFlow.verify("ana@test.ph", fresh);
    }

    @Test
    void codesExpireAfterFifteenMinutes() {
        String code = k.authFlow.signup(signup("ana@test.ph", "Secret123")).devCode();
        k.securityClock.advanceMinutes(16);
        assertEquals(400, fails(() -> k.authFlow.verify("ana@test.ph", code)).getStatus());
    }

    @Test
    void aSignupCodeCannotResetAPasswordAndViceVersa() {
        String signupCode = k.authFlow.signup(signup("ana@test.ph", "Secret123")).devCode();
        assertEquals(400, fails(() -> k.authFlow.resetPassword("ana@test.ph", signupCode, "Another123")).getStatus());
        String resetCode = k.authFlow.forgotPassword("maria@test.ph").devCode();
        assertEquals(400, fails(() -> k.authFlow.verify("maria@test.ph", resetCode)).getStatus());
    }

    /* ---- forgot / reset ---- */

    @Test
    void forgotPasswordAnswersTheSameForUnknownEmails() {
        AuthService.CodeIssued known = k.authFlow.forgotPassword("maria@test.ph");
        AuthService.CodeIssued unknown = k.authFlow.forgotPassword("ghost@test.ph");
        assertNotNull(known.devCode());
        assertNull(unknown.devCode());
        assertEquals(1, k.sender.sent.stream().filter(s -> s.startsWith("reset:")).count());   // nothing was "sent" to the unknown address
    }

    @Test
    void resetWithTheCodeChangesThePasswordAndClearsLockout() {
        for (int i = 0; i < 5; i++) try { k.authFlow.login("maria@test.ph", "bad"); } catch (ApiException e) { /* locking */ }
        String code = k.authFlow.forgotPassword("maria@test.ph").devCode();
        k.authFlow.checkResetCode("maria@test.ph", code);
        k.authFlow.resetPassword("maria@test.ph", code, "BrandNew123");
        assertEquals("U-1001", k.authFlow.login("maria@test.ph", "BrandNew123").getId());
        assertEquals(401, fails(() -> k.authFlow.login("maria@test.ph", "Resident#2026")).getStatus());
        assertEquals(400, fails(() -> k.authFlow.resetPassword("maria@test.ph", code, "Again12345")).getStatus());   // single use
    }

    @Test
    void resetEnforcesThePasswordPolicy() {
        String code = k.authFlow.forgotPassword("maria@test.ph").devCode();
        assertEquals(400, fails(() -> k.authFlow.resetPassword("maria@test.ph", code, "weak")).getStatus());
    }

    /* ---- change password ---- */

    @Test
    void changePasswordNeedsTheCurrentOneAndAClearsTheTemporaryFlag() {
        k.maria.setMustChangePassword(true);
        assertEquals(400, fails(() -> k.authFlow.changePassword(k.maria, "wrong", "Better1234")).getStatus());
        assertEquals(400, fails(() -> k.authFlow.changePassword(k.maria, "Resident#2026", "Resident#2026")).getStatus());
        assertEquals(400, fails(() -> k.authFlow.changePassword(k.maria, "Resident#2026", "weak")).getStatus());
        k.authFlow.changePassword(k.maria, "Resident#2026", "Better1234");
        assertFalse(k.users.findById("U-1001").orElseThrow().getMustChangePassword());
        assertEquals("U-1001", k.authFlow.login("maria@test.ph", "Better1234").getId());
    }
}
