package am.trade.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for handling date and time operations with IST timezone
 */
public class DateTimeUtil {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Get current LocalDateTime in IST timezone
     * 
     * @return Current LocalDateTime in IST
     */
    public static LocalDateTime getCurrentISTDateTime() {
        return LocalDateTime.now(IST_ZONE);
    }
    
    /**
     * Convert UTC LocalDateTime to IST LocalDateTime
     * 
     * @param utcDateTime LocalDateTime in UTC
     * @return LocalDateTime in IST
     */
    public static LocalDateTime convertUTCtoIST(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        ZonedDateTime utcZoned = utcDateTime.atZone(ZoneId.of("UTC"));
        return utcZoned.withZoneSameInstant(IST_ZONE).toLocalDateTime();
    }
    
    /**
     * Convert IST LocalDateTime to UTC LocalDateTime
     * 
     * @param istDateTime LocalDateTime in IST
     * @return LocalDateTime in UTC
     */
    public static LocalDateTime convertISTtoUTC(LocalDateTime istDateTime) {
        if (istDateTime == null) {
            return null;
        }
        ZonedDateTime istZoned = istDateTime.atZone(IST_ZONE);
        return istZoned.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
    }
    
    /**
     * Format LocalDateTime to string using default format (yyyy-MM-dd HH:mm:ss)
     * 
     * @param dateTime LocalDateTime to format
     * @return Formatted date time string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_FORMATTER) : null;
    }
    
    /**
     * Format LocalDateTime to string using specified format
     * 
     * @param dateTime LocalDateTime to format
     * @param pattern Format pattern
     * @return Formatted date time string
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
}
