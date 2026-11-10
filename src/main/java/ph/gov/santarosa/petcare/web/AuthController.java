package ph.gov.santarosa.petcare.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ph.gov.santarosa.petcare.domain.AppUser;
import ph.gov.santarosa.petcare.security.CurrentUser;
import ph.gov.santarosa.petcare.service.AuthService;
import ph.gov.santarosa.petcare.service.StateAssembler;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record Login(String email, String password) { }
    public record EmailBody(String email) { }
    public record Verify(String email, String code) { }
    public record Reset(String email, String code, String newPassword) { }
    public record ChangePassword(String currentPassword, String newPassword) { }

    private final AuthService auth;
    private final CurrentUser current;
    private final SecurityContextRepository contextRepository;

    public AuthController(AuthService auth, CurrentUser current, SecurityContextRepository contextRepository) {
        this.auth = auth; this.current = current; this.contextRepository = contextRepository;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Login body, HttpServletRequest request, HttpServletResponse response) {
        AppUser u = auth.login(body.email(), body.password());
        // Fresh session on every login (prevents session fixation), holding only the user id.
        HttpSession old = request.getSession(false);
        if (old != null) old.invalidate();
        request.getSession(true);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                u.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().toUpperCase())));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        return Map.of("user", StateAssembler.publicUser(u));
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s != null) s.invalidate();
        SecurityContextHolder.clearContext();
        return Map.of("ok", true);
    }

    @GetMapping("/me")
    public Map<String, Object> me() { return Map.of("user", StateAssembler.publicUser(current.require())); }

    @PostMapping("/signup")
    public Map<String, Object> signup(@RequestBody AuthService.SignupRequest body) { return issued(auth.signup(body)); }

    @PostMapping("/verify")
    public Map<String, Object> verify(@RequestBody Verify body) { auth.verify(body.email(), body.code()); return Map.of("ok", true); }

    @PostMapping("/resend-code")
    public Map<String, Object> resend(@RequestBody EmailBody body) { return issued(auth.resendSignupCode(body.email())); }

    @PostMapping("/forgot")
    public Map<String, Object> forgot(@RequestBody EmailBody body) { return issued(auth.forgotPassword(body.email())); }

    @PostMapping("/reset/check")
    public Map<String, Object> resetCheck(@RequestBody Verify body) { auth.checkResetCode(body.email(), body.code()); return Map.of("ok", true); }

    @PostMapping("/reset")
    public Map<String, Object> reset(@RequestBody Reset body) { auth.resetPassword(body.email(), body.code(), body.newPassword()); return Map.of("ok", true); }

    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody ChangePassword body) {
        auth.changePassword(current.require(), body.currentPassword(), body.newPassword());
        return Map.of("ok", true);
    }

    private static Map<String, Object> issued(AuthService.CodeIssued c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("email", c.email());
        if (c.devCode() != null) m.put("devCode", c.devCode()); // explicit local mock-otp profile only
        return m;
    }
}
