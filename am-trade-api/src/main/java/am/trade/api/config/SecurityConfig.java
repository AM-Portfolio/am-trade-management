package am.trade.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Spring Security Configuration for Trade Management Service
 * 
 * Security Model: Zero Trust
 * - Validates JWT signature using INTERNAL_JWT_SECRET
 * - Trusts API Gateway ONLY if it presents a valid, signed Service Token
 * - Protected endpoints require valid JWT with correct signature
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Value("${jwt.secret}")
        private String jwtSecret;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // Disable CSRF (stateless REST API with JWT)
                                .csrf(csrf -> csrf.disable())

                                // Stateless session management (no cookies, JWT-based)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Configure authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                // ✅ PUBLIC ENDPOINTS - No authentication required
                                                .requestMatchers(
                                                                "/actuator/health", // Docker health check
                                                                "/actuator/health/live", // Kubernetes liveness probe
                                                                "/actuator/health/ready", // Kubernetes readiness probe
                                                                "/swagger-ui/**", // Swagger API documentation
                                                                "/v3/api-docs/**", // OpenAPI specification
                                                                "/v3/api-docs.yaml" // OpenAPI YAML
                                                ).permitAll()

                                                // ✅ PROTECTED ENDPOINTS - Require valid JWT
                                                .requestMatchers(
                                                                "/api/v1/trades/**" // All trade operations
                                                ).authenticated()

                                                // ❌ Deny all other endpoints (fail secure)
                                                .anyRequest().denyAll())

                                // ✅ ZERO TRUST: Enforce JWT Validation
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())))

                                // Disable HTTP Basic authentication (not needed, using JWT)
                                .httpBasic(basic -> basic.disable())

                                // Disable form login (API Gateway handles authentication)
                                .formLogin(form -> form.disable());

                return http.build();
        }

        @Bean
        public JwtDecoder jwtDecoder() {
                // Use HS256 (Symmetric Key) to match Auth Service
                SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
                return NimbusJwtDecoder.withSecretKey(key).build();
        }
}
