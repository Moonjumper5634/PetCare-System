package ph.gov.santarosa.petcare.security;

import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Who may reach what.
 *  - Static files (the website) are public. Everything under /api needs a signed-in session, except sign-up,
 *    login, verification and password-reset calls.
 *  - /api/admin/** is for administrators only. Finer rules (a resident sees only their own records, staff-only
 *    actions, state rules) are enforced again inside the services, using the role stored in the database.
 *  - The session is an HttpOnly cookie; the browser never holds a token or a password.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(10); }

    @Bean
    public SecurityContextRepository securityContextRepository() { return new HttpSessionSecurityContextRepository(); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository repo) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                       // replaced by RequestedWithFilter + SameSite cookie (see that class)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())                 // /api/auth/logout is handled by AuthController
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .securityContext(sc -> sc.securityContextRepository(repo))
            .headers(h -> h
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                .contentSecurityPolicy(c -> c.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com data:; img-src 'self' data: https://images.unsplash.com; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'")))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST,
                        "/api/auth/login", "/api/auth/logout", "/api/auth/signup", "/api/auth/verify",
                        "/api/auth/resend-code", "/api/auth/forgot", "/api/auth/reset/check", "/api/auth/reset").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/dev/db-status").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/dev/**").hasRole("ADMIN")       // dev-only endpoints (exist only in the dev profile)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> json(res, 401, "Please sign in."))
                .accessDeniedHandler((req, res, e) -> json(res, 403, "You do not have permission to do that.")))
            .addFilterBefore(new RequestedWithFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new PublicAuthRateLimitFilter(), RequestedWithFilter.class);
        return http.build();
    }

    private static void json(jakarta.servlet.http.HttpServletResponse res, int status, String message) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getOutputStream().write(("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8));
    }
}
