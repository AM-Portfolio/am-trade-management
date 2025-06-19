package am.trade.common.util;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Utility class for common validation operations
 */
@Component
public class ValidationUtils {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    /**
     * Check if a string is null or empty
     */
    public boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }
    
    /**
     * Check if a string is not null and not empty
     */
    public boolean isNotEmpty(String str) {
        return StringUtils.isNotEmpty(str);
    }
    
    /**
     * Check if a string is null, empty or only whitespace
     */
    public boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }
    
    /**
     * Check if a string is not null, not empty and not only whitespace
     */
    public boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }
    
    /**
     * Check if a collection is null or empty
     */
    public boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * Check if a collection is not null and not empty
     */
    public boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
    
    /**
     * Check if a map is null or empty
     */
    public boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
    
    /**
     * Check if a map is not null and not empty
     */
    public boolean isNotEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }
    
    /**
     * Check if a string is a valid email format
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Check if a number is within range (inclusive)
     */
    public boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * Check if a number is within range (inclusive)
     */
    public boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
}
