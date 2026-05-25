package am.trade.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Security configuration for AM Trade Management
 * 
 * Security Model: Gateway-Enforced Authentication
 * This service is an internal microservice deployed exclusively behind am-gateway, 
 * which validates JWTs and enforces auth at the edge before forwarding requests.
 */
@Configuration
@EnableWebSecurity
@Profile("!local")
public class SecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // am-trade-management is behind am-gateway which enforces JWT at the edge.
                // All endpoints are open at the service layer — firewall blocks direct access.
                .requestMatchers(AntPathRequestMatcher.antMatcher("/**")).permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }
}
