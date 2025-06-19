package am.trade.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

/**
 * Utility class for date and time operations
 */
@Component
public class DateTimeUtils {
    
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    
    /**
     * Format a LocalDate to string using default format
     */
    public String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }
    
    /**
     * Format a LocalDate to string using specified format
     */
    public String formatDate(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * Format a LocalDateTime to string using default format
     */
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
    }
    
    /**
     * Format a LocalDateTime to string using specified format
     */
    public String formatDateTime(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * Parse a string to LocalDate using default format
     */
    public LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }
    
    /**
     * Parse a string to LocalDate using specified format
     */
    public LocalDate parseDate(String dateStr, String pattern) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * Parse a string to LocalDateTime using default format
     */
    public LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
    }
    
    /**
     * Parse a string to LocalDateTime using specified format
     */
    public LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * Convert epoch millis to LocalDateTime
     */
    public LocalDateTime fromEpochMillis(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(DEFAULT_ZONE_ID).toLocalDateTime();
    }
    
    /**
     * Convert LocalDateTime to epoch millis
     */
    public long toEpochMillis(LocalDateTime dateTime) {
        return dateTime.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
    }
}
