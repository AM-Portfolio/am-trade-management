package am.trade.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class CorsIntegrationTest {

    @Test
    public void testCorsConfigurationMatchesPortfolio() throws Exception {
        ApiAutoConfiguration config = new ApiAutoConfiguration();
        CorsFilter filter = config.corsFilter(java.util.Arrays.asList("http://localhost:*", "https://*"));
        
        assertNotNull(filter, "CorsFilter bean should be created");
        
        // Extract the source to verify properties
        Field sourceField = CorsFilter.class.getDeclaredField("configSource");
        sourceField.setAccessible(true);
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) sourceField.get(filter);
        
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/v1/trades/details");
        CorsConfiguration corsConfig = source.getCorsConfiguration(request);
        
        assertNotNull(corsConfig, "Should have CORS configured for /v1/trades/details");
        
        assertTrue(corsConfig.getAllowCredentials(), "Should allow credentials");
        assertTrue(corsConfig.getAllowedOriginPatterns().contains("https://am-dev.asrax.in") || 
                  corsConfig.getAllowedOriginPatterns().contains("https://*"), 
                  "Should allow production origins");
        assertTrue(corsConfig.getAllowedOriginPatterns().contains("http://localhost:*"), "Should allow local origins");
        assertTrue(corsConfig.getAllowedMethods().contains("*"), "Should allow all methods");
    }
}
