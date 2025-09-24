package am.trade.common.config;

import java.util.TimeZone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

/**
 * Configuration class to set the default timezone to IST (Indian Standard Time)
 */
@Configuration
public class TimeZoneConfig {

    /**
     * Set the default timezone for the JVM to IST
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        return jacksonObjectMapperBuilder -> 
            jacksonObjectMapperBuilder.timeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    }
    
    /**
     * Initialize the application with IST timezone
     * 
     * @return The TimeZone that was set as default
     */
    @Bean
    public TimeZone setDefaultTimeZone() {
        TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
        TimeZone.setDefault(istTimeZone);
        return istTimeZone;
    }
}
