package ph.gov.santarosa.petcare.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.filter.OncePerRequestFilter;

/** Small single-instance guard against automated login/code-sending abuse. */
public class PublicAuthRateLimitFilter extends OncePerRequestFilter {
    private static final int LIMIT=20;
    private static final long WINDOW_MS=Duration.ofMinutes(15).toMillis();
    private static final Set<String> LIMITED=Set.of("/api/auth/login","/api/auth/signup","/api/auth/resend-code","/api/auth/forgot","/api/auth/reset/check","/api/auth/reset");
    private final ConcurrentHashMap<String,Window> windows=new ConcurrentHashMap<>();
    private record Window(long started, AtomicInteger count) { }

    @Override protected boolean shouldNotFilter(HttpServletRequest request){return !"POST".equals(request.getMethod())||!LIMITED.contains(request.getRequestURI());}

    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws ServletException,IOException {
        long now=System.currentTimeMillis();String key=clientIp(request)+"|"+request.getRequestURI();
        Window w=windows.compute(key,(k,old)->old==null||now-old.started()>WINDOW_MS?new Window(now,new AtomicInteger(1)):increment(old));
        if(w.count().get()>LIMIT){response.setStatus(429);response.setContentType("application/json");response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After",String.valueOf(Math.max(1,(w.started()+WINDOW_MS-now)/1000)));
            response.getOutputStream().write("{\"error\":\"Too many attempts. Please wait and try again.\"}".getBytes(StandardCharsets.UTF_8));return;}
        if(windows.size()>10_000)windows.entrySet().removeIf(e->now-e.getValue().started()>WINDOW_MS);
        chain.doFilter(request,response);
    }
    private static Window increment(Window w){w.count().incrementAndGet();return w;}
    private static String clientIp(HttpServletRequest r){String forwarded=r.getHeader("X-Forwarded-For");return forwarded==null||forwarded.isBlank()?r.getRemoteAddr():forwarded.split(",",2)[0].trim();}
}
