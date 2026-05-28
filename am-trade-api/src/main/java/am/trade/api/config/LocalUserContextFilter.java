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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;

/**
 * Local development helper: Extracts user ID from JWT if present,
 * otherwise binds a default user to {@link UserContext}.
 */
@Component
@Profile("local")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class LocalUserContextFilter extends OncePerRequestFilter {

    @Value("${am.trade.security.local-default-user-id:local-dev-user}")
    private String localDefaultUserId;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        boolean hasBearer = authHeader != null && authHeader.startsWith("Bearer ");
        
        // In local mode, if UserContext is not populated, attempt to extract it from the real token
        if (UserContext.getUserId() == null) {
            String extractedUserId = null;
            if (hasBearer) {
                try {
                    String token = authHeader.substring(7);
                    String[] parts = token.split("\\.");
                    if (parts.length >= 2) {
                        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                        JsonNode rootNode = objectMapper.readTree(payload);
                        if (rootNode.has("sub")) {
                            extractedUserId = rootNode.get("sub").asText();
                        } else if (rootNode.has("userId")) {
                            extractedUserId = rootNode.get("userId").asText();
                        } else if (rootNode.has("user_id")) {
                            extractedUserId = rootNode.get("user_id").asText();
                        }
                    }
                } catch (Exception e) {
                    // Ignore token parsing errors and fallback
                }
            }
            
            if (extractedUserId != null && !extractedUserId.isEmpty()) {
                UserContext.setUserId(extractedUserId);
                UserContext.setEmail(extractedUserId + "@local.dev");
            } else {
                UserContext.setUserId(localDefaultUserId);
                UserContext.setEmail(localDefaultUserId + "@local.dev");
            }
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clear the context to prevent thread-local leaks
            UserContext.clear();
        }
    }
}
