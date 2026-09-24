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
    private static final Pattern ISIN_PATTERN = Pattern.compile("^[A-Z]{2}[A-Z0-9]{9}[0-9]$");
    
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
    
    /**
     * Check if a string is a valid International Securities Identification Number (ISIN).
     * An ISIN is 12 characters, starting with a 2-letter country code, 
     * followed by a 9-character alphanumeric security identifier, 
     * and a final numeric check digit computed via the Luhn algorithm.
     */
    public boolean isValidIsin(String isin) {
        if (isin == null || !ISIN_PATTERN.matcher(isin).matches()) {
            return false;
        }

        // Luhn algorithm for ISIN:
        // Convert letters to numbers (A=10, B=11 ... Z=35)
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            char c = isin.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
            } else {
                digits.append(Character.getNumericValue(c));
            }
        }

        int sum = 0;
        boolean doubleDigit = true; // Right-to-left, the rightmost digit before check digit is doubled
        
        // Process from right to left
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(digits.charAt(i));
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9; // equivalent to sum of its digits (e.g., 18 -> 1+8 = 9)
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }

        int expectedCheckDigit = (10 - (sum % 10)) % 10;
        int actualCheckDigit = Character.getNumericValue(isin.charAt(11));

        return expectedCheckDigit == actualCheckDigit;
    }
}
