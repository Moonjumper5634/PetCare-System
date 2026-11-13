package ph.gov.santarosa.petcare.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Cross-site request forgery defence for the JSON API: every state-changing /api request must carry the custom
 * header "X-Requested-With: PetCARE". A web page on another site cannot add a custom header to a request to this
 * server (that would need a CORS preflight, and CORS is not enabled), so forged requests are refused.
 * Together with the SameSite=Lax session cookie this replaces Spring's token-based CSRF for this same-origin app.
 */
public class RequestedWithFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Requested-With";
    public static final String VALUE = "PetCARE";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String m = request.getMethod();
        boolean safe = "GET".equals(m) || "HEAD".equals(m) || "OPTIONS".equals(m);
        if (!safe && request.getRequestURI().startsWith("/api/") && !VALUE.equals(request.getHeader(HEADER))) {
            response.setStatus(403);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getOutputStream().write("{\"error\":\"Request rejected.\"}".getBytes(StandardCharsets.UTF_8));
            return;
        }
        chain.doFilter(request, response);
    }
}
