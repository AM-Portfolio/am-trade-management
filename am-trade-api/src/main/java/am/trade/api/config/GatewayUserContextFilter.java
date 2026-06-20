package am.trade.api.config;

import com.am.security.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Fallback filter to extract User ID from the Gateway's X-User-Id header
 * in non-local environments where the Gateway strips the Authorization header.
 * 
 * This is a local fix in am-trade-management until am-core-services natively
 * supports Gateway-Offloaded Authentication.
 */
@Component
@Profile("!local")
@Order(Ordered.HIGHEST_PRECEDENCE + 11)
public class GatewayUserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // If UserContext is empty (meaning the core-services filter failed to find an Auth header)
        if (UserContext.getUserId() == null) {
            String gatewayUserId = request.getHeader("X-User-Id");
            if (gatewayUserId == null || gatewayUserId.isEmpty()) {
                gatewayUserId = request.getHeader("X-User-ID");
            }
            
            if (gatewayUserId != null && !gatewayUserId.isEmpty()) {
                UserContext.setUserId(gatewayUserId);
            }
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Context will be cleared by the downstream core-services filter,
            // but we clear it here as well for safety.
            UserContext.clear();
        }
    }
}