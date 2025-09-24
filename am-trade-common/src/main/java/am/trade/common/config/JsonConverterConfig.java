package am.trade.common.config;

import am.trade.common.util.JsonConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to create a JsonConverter bean
 */
@Configuration
public class JsonConverterConfig {

    /**
     * Creates a JsonConverter bean for dependency injection
     * @return JsonConverter instance
     */
    @Bean
    public JsonConverter jsonConverter() {
        return new JsonConverter();
    }
}
