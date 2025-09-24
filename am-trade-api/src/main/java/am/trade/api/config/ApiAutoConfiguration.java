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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnProperty(prefix = "am.trade.api", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "am.trade.api")
@Import({TradeServicesAutoConfiguration.class, DashboardAutoConfiguration.class})
public class ApiAutoConfiguration {
    
    /**
     * Configures CORS for the API to allow cross-origin requests from the frontend
     * @return WebMvcConfigurer with CORS configuration
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000") // Frontend URL
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600); // 1 hour max age
            }
        };
    }
}
