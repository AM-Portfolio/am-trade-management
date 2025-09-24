package am.trade.common.config;

import am.trade.common.jackson.TradeJacksonModule;
import am.trade.common.jackson.TradeManagementJacksonModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Configuration for Jackson JSON processing
 */
@Configuration
public class JacksonConfig {
    
    /**
     * Configure ObjectMapper with custom modules
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // Register our custom modules
        objectMapper.registerModule(new TradeJacksonModule());
        objectMapper.registerModule(new TradeManagementJacksonModule());
        
        return objectMapper;
    }
}
