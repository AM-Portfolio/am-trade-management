package am.trade.api.config;

import com.am.security.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Local development helper: when no Bearer token is present, binds a default user to
 * {@link UserContext} so endpoints using {@code getUserIdOrThrow()} work without manual JWT setup.
 */
@Component
@Profile("local")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class LocalUserContextFilter extends OncePerRequestFilter {

    @Value("${am.trade.security.local-default-user-id:local-dev-user}")
    private String localDefaultUserId;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        boolean hasBearer = authHeader != null && authHeader.startsWith("Bearer ");
        
        // In local mode, if UserContext is not populated (e.g. Gateway is bypassed or mock token used),
        // we always inject the local default user.
        if (UserContext.getUserId() == null) {
            UserContext.setUserId(localDefaultUserId);
            UserContext.setEmail(localDefaultUserId + "@local.dev");
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clear the context to prevent thread-local leaks
            UserContext.clear();
        }
    }
}
