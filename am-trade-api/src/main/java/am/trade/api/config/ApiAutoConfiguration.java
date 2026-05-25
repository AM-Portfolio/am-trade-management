package am.trade.api.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import am.trade.dashboard.config.DashboardAutoConfiguration;
import am.trade.services.config.TradeServicesAutoConfiguration;

/**
 * Auto-configuration for API module
 */
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@AutoConfiguration
@ConditionalOnProperty(prefix = "am.trade.api", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "am.trade.api")
@Import({TradeServicesAutoConfiguration.class, DashboardAutoConfiguration.class})
public class ApiAutoConfiguration {
    
    /**
     * Configures CORS for the API to allow cross-origin requests from the frontend
     * Uses CorsFilter which integrates better with Spring Security than WebMvcConfigurer
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("http://localhost:*"); // Allow all localhost ports
        config.addAllowedOriginPattern("https://localhost:*"); // Allow local HTTPS
        config.addAllowedOriginPattern("https://*.replit.dev");
        config.addAllowedOriginPattern("https://*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
