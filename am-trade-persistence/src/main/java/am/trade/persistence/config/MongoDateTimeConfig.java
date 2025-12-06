package am.trade.persistence.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.lang.NonNull;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.common.models.enums.FundamentalEntryReason;
import am.trade.common.models.enums.TechnicalEntryReason;
import am.trade.common.models.enums.TradeBehaviorPattern;

/**
 * MongoDB configuration for handling LocalDateTime in IST timezone and flexible enum conversions
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
     * Converter from String to TechnicalEntryReason (for old data format)
     */
    public static class StringToTechnicalEntryReasonConverter implements Converter<String, TechnicalEntryReason> {
        @Override
        public TechnicalEntryReason convert(@NonNull String source) {
            return TechnicalEntryReason.fromCode(source, null);
        }
    }

    /**
     * Converter from Map to TechnicalEntryReason (for new data format)
     */
    public static class MapToTechnicalEntryReasonConverter implements Converter<Map<String, String>, TechnicalEntryReason> {
        @Override
        public TechnicalEntryReason convert(@NonNull Map<String, String> source) {
            String code = source.get("code");
            String description = source.get("description");
            return TechnicalEntryReason.fromCode(code, description);
        }
    }

    /**
     * Converter from String to FundamentalEntryReason (for old data format)
     */
    public static class StringToFundamentalEntryReasonConverter implements Converter<String, FundamentalEntryReason> {
        @Override
        public FundamentalEntryReason convert(@NonNull String source) {
            return FundamentalEntryReason.fromCode(source, null);
        }
    }

    /**
     * Converter from Map to FundamentalEntryReason (for new data format)
     */
    public static class MapToFundamentalEntryReasonConverter implements Converter<Map<String, String>, FundamentalEntryReason> {
        @Override
        public FundamentalEntryReason convert(@NonNull Map<String, String> source) {
            String code = source.get("code");
            String description = source.get("description");
            return FundamentalEntryReason.fromCode(code, description);
        }
    }

    /**
     * Converter from String to EntryPsychology (for old data format)
     */
    public static class StringToEntryPsychologyConverter implements Converter<String, EntryPsychology> {
        @Override
        public EntryPsychology convert(@NonNull String source) {
            return EntryPsychology.fromCode(source, null);
        }
    }

    /**
     * Converter from Map to EntryPsychology (for new data format)
     */
    public static class MapToEntryPsychologyConverter implements Converter<Map<String, String>, EntryPsychology> {
        @Override
        public EntryPsychology convert(@NonNull Map<String, String> source) {
            String code = source.get("code");
            String description = source.get("description");
            return EntryPsychology.fromCode(code, description);
        }
    }

    /**
     * Converter from String to ExitPsychology (for old data format)
     */
    public static class StringToExitPsychologyConverter implements Converter<String, ExitPsychology> {
        @Override
        public ExitPsychology convert(@NonNull String source) {
            return ExitPsychology.fromCode(source, null);
        }
    }

    /**
     * Converter from Map to ExitPsychology (for new data format)
     */
    public static class MapToExitPsychologyConverter implements Converter<Map<String, String>, ExitPsychology> {
        @Override
        public ExitPsychology convert(@NonNull Map<String, String> source) {
            String code = source.get("code");
            String description = source.get("description");
            return ExitPsychology.fromCode(code, description);
        }
    }

    /**
     * Converter from String to TradeBehaviorPattern (for old data format)
     */
    public static class StringToTradeBehaviorPatternConverter implements Converter<String, TradeBehaviorPattern> {
        @Override
        public TradeBehaviorPattern convert(@NonNull String source) {
            return TradeBehaviorPattern.fromCode(source, null);
        }
    }

    /**
     * Converter from Map to TradeBehaviorPattern (for new data format)
     */
    public static class MapToTradeBehaviorPatternConverter implements Converter<Map<String, String>, TradeBehaviorPattern> {
        @Override
        public TradeBehaviorPattern convert(@NonNull Map<String, String> source) {
            String code = source.get("code");
            String description = source.get("description");
            return TradeBehaviorPattern.fromCode(code, description);
        }
    }

    /**
     * Register custom converters for MongoDB including DateTime and flexible enum converters
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        // DateTime converters
        converters.add(new LocalDateTimeToDateConverter());
        converters.add(new DateToLocalDateTimeConverter());
        // Flexible enum converters - String to Enum (for old data)
        converters.add(new StringToTechnicalEntryReasonConverter());
        converters.add(new StringToFundamentalEntryReasonConverter());
        converters.add(new StringToEntryPsychologyConverter());
        converters.add(new StringToExitPsychologyConverter());
        converters.add(new StringToTradeBehaviorPatternConverter());
        // Flexible enum converters - Map to Enum (for new data with @JsonValue)
        converters.add(new MapToTechnicalEntryReasonConverter());
        converters.add(new MapToFundamentalEntryReasonConverter());
        converters.add(new MapToEntryPsychologyConverter());
        converters.add(new MapToExitPsychologyConverter());
        converters.add(new MapToTradeBehaviorPatternConverter());
        return new MongoCustomConversions(converters);
    }
}
