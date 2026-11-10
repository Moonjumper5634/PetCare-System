package ph.gov.santarosa.petcare.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class PublicAuthRateLimitFilterTest {
    @Test
    void blocksTwentyFirstAttemptForSameAddressAndEndpoint() throws Exception {
        PublicAuthRateLimitFilter filter = new PublicAuthRateLimitFilter();
        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest request = request("/api/auth/login");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            assertEquals(200, response.getStatus());
        }

        MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(request("/api/auth/login"), blocked, new MockFilterChain());
        assertEquals(429, blocked.getStatus());
        assertTrue(Integer.parseInt(blocked.getHeader("Retry-After")) > 0);
    }

    @Test
    void leavesUnrelatedRequestsAlone() throws Exception {
        PublicAuthRateLimitFilter filter = new PublicAuthRateLimitFilter();
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request("/api/bootstrap"), response, new MockFilterChain());
        assertEquals(200, response.getStatus());
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr("203.0.113.10");
        return request;
    }
}
