package am.trade.persistence.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.lang.NonNull;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

/**
 * MongoDB configuration for handling LocalDateTime in IST timezone
 */
@Configuration
public class MongoDateTimeConfig {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    /**
     * Custom converter to convert LocalDateTime to Date in IST timezone
     */
    public static class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
        @Override
        @NonNull
        public Date convert(@NonNull LocalDateTime source) {
            return Date.from(source.atZone(IST_ZONE).toInstant());
        }
    }

    /**
     * Custom converter to convert Date to LocalDateTime in IST timezone
     */
    public static class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
        @Override
        @NonNull
        public LocalDateTime convert(@NonNull Date source) {
            return ZonedDateTime.ofInstant(source.toInstant(), IST_ZONE).toLocalDateTime();
        }
    }

    /**
     * Register custom converters for MongoDB
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new LocalDateTimeToDateConverter());
        converters.add(new DateToLocalDateTimeConverter());
        return new MongoCustomConversions(converters);
    }
}
