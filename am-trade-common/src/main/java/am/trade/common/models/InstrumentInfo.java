package am.trade.common.models;

import am.trade.common.models.enums.Exchange;
import am.trade.common.models.enums.IndexType;
import am.trade.common.models.enums.MarketSegment;
import am.trade.common.models.enums.SeriesType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing instrument information for various market instruments
 * including equities, futures, and options
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentInfo {
    // Basic instrument identification
    private String symbol;
    private String isin;
    private String rawSymbol; // The original unparsed symbol string
    
    // Market classification
    private Exchange exchange;
    private MarketSegment segment;
    private SeriesType series;
    
    // For indices
    private IndexType indexType;
    
    // For derivatives (options and futures)
    private DerivativeInfo derivativeInfo;
    
    // Additional fields
    private String description;
    private String currency;
    private String lotSize;
    
    // Regular expressions for parsing different symbol formats
    private static final Pattern FUTURES_PATTERN = Pattern.compile("([A-Z]+)(\\d{2})([A-Z]{3})FUT");
    private static final Pattern OPTIONS_PATTERN = Pattern.compile("([A-Z]+)(\\d{2})(\\d{2})(\\d{2})(\\d+)([CP]E)");
    
    /**
     * Factory method to create an InstrumentInfo from a raw symbol string
     * 
     * @param rawSymbol The raw symbol string (e.g., "MARUTI20SEPFUT", "BANKNIFTY2091722500CE")
     * @return A populated InstrumentInfo object
     */
    public static InstrumentInfo fromRawSymbol(String rawSymbol) {
        if (rawSymbol == null || rawSymbol.isEmpty()) {
            return null;
        }
        
        InstrumentInfo info = new InstrumentInfo();
        info.setRawSymbol(rawSymbol);
        
        // Check if it's a futures contract
        Matcher futuresMatcher = FUTURES_PATTERN.matcher(rawSymbol);
        if (futuresMatcher.matches()) {
            return parseFuturesSymbol(rawSymbol, futuresMatcher, info);
        }
        
        // Check if it's an options contract
        Matcher optionsMatcher = OPTIONS_PATTERN.matcher(rawSymbol);
        if (optionsMatcher.matches()) {
            return parseOptionsSymbol(rawSymbol, optionsMatcher, info);
        }
        
        // If it's neither futures nor options, treat as equity or index
        return parseEquityOrIndexSymbol(rawSymbol, info);
    }
    
    /**
     * Parse a futures symbol like "MARUTI20SEPFUT"
     */
    private static InstrumentInfo parseFuturesSymbol(String rawSymbol, Matcher matcher, InstrumentInfo info) {
        String baseSymbol = matcher.group(1);
        String yearStr = matcher.group(2);
        String monthStr = matcher.group(3);
        
        info.setSymbol(baseSymbol);
        
        // Check if the base symbol is an index
        IndexType indexType = IndexType.fromSymbol(baseSymbol);
        if (indexType != IndexType.UNKNOWN) {
            info.setIndexType(indexType);
            info.setSegment(MarketSegment.INDEX_FUTURES);
        } else {
            info.setSegment(MarketSegment.EQUITY_FUTURES);
        }
        
        // Create derivative info
        DerivativeInfo derivativeInfo = new DerivativeInfo();
        derivativeInfo.setUnderlyingSymbol(baseSymbol);
        derivativeInfo.setFutureType("MONTHLY"); // Default assumption
        derivativeInfo.setIsCashSettled(true);    // Default assumption
        
        // Parse expiry date
        try {
            int year = 2000 + Integer.parseInt(yearStr); // Assuming 2-digit year
            Month month = parseMonth(monthStr);
            // Set to last Thursday of the month (common for futures)
            LocalDate expiryDate = getLastThursdayOfMonth(year, month);
            derivativeInfo.setExpiryDate(expiryDate);
        } catch (Exception e) {
            // If date parsing fails, leave it null
        }
        
        info.setDerivativeInfo(derivativeInfo);
        return info;
    }
    
    /**
     * Parse an options symbol like "BANKNIFTY2091722500CE"
     */
    private static InstrumentInfo parseOptionsSymbol(String rawSymbol, Matcher matcher, InstrumentInfo info) {
        String baseSymbol = matcher.group(1);
        String yearStr = matcher.group(2);
        String monthStr = matcher.group(3);
        String dayStr = matcher.group(4);
        String strikePriceStr = matcher.group(5);
        String optionTypeStr = matcher.group(6);
        
        info.setSymbol(baseSymbol);
        
        // Check if the base symbol is an index
        IndexType indexType = IndexType.fromSymbol(baseSymbol);
        if (indexType != IndexType.UNKNOWN) {
            info.setIndexType(indexType);
            info.setSegment(MarketSegment.INDEX_OPTIONS);
        } else {
            info.setSegment(MarketSegment.EQUITY_OPTIONS);
        }
        
        // Create derivative info
        DerivativeInfo derivativeInfo = new DerivativeInfo();
        derivativeInfo.setUnderlyingSymbol(baseSymbol);
        derivativeInfo.setIsCall(optionTypeStr.startsWith("C")); // CE for Call, PE for Put
        derivativeInfo.setIsEuropean(true); // Default assumption for Indian markets
        derivativeInfo.setIsCashSettled(true); // Default assumption
        
        // Parse strike price
        try {
            BigDecimal strikePrice = new BigDecimal(strikePriceStr);
            derivativeInfo.setStrikePrice(strikePrice);
        } catch (NumberFormatException e) {
            // If strike price parsing fails, leave it null
        }
        
        // Parse expiry date
        try {
            int year = 2000 + Integer.parseInt(yearStr); // Assuming 2-digit year
            int month = Integer.parseInt(monthStr);
            int day = Integer.parseInt(dayStr);
            LocalDate expiryDate = LocalDate.of(year, month, day);
            derivativeInfo.setExpiryDate(expiryDate);
        } catch (Exception e) {
            // If date parsing fails, leave it null
        }
        
        info.setDerivativeInfo(derivativeInfo);
        return info;
    }
    
    /**
     * Parse an equity or index symbol like "NIFTY" or "RELIANCE"
     */
    private static InstrumentInfo parseEquityOrIndexSymbol(String rawSymbol, InstrumentInfo info) {
        info.setSymbol(rawSymbol);
        
        // Check if it's an index
        IndexType indexType = IndexType.fromSymbol(rawSymbol);
        if (indexType != IndexType.UNKNOWN) {
            info.setIndexType(indexType);
            info.setSegment(MarketSegment.INDEX);
        } else {
            info.setSegment(MarketSegment.EQUITY);
        }
        
        return info;
    }
    
    /**
     * Parse month abbreviation to Month enum
     */
    private static Month parseMonth(String monthStr) {
        switch (monthStr.toUpperCase()) {
            case "JAN": return Month.JANUARY;
            case "FEB": return Month.FEBRUARY;
            case "MAR": return Month.MARCH;
            case "APR": return Month.APRIL;
            case "MAY": return Month.MAY;
            case "JUN": return Month.JUNE;
            case "JUL": return Month.JULY;
            case "AUG": return Month.AUGUST;
            case "SEP": return Month.SEPTEMBER;
            case "OCT": return Month.OCTOBER;
            case "NOV": return Month.NOVEMBER;
            case "DEC": return Month.DECEMBER;
            default: throw new IllegalArgumentException("Invalid month: " + monthStr);
        }
    }
    
    /**
     * Calculate the last Thursday of a given month
     */
    private static LocalDate getLastThursdayOfMonth(int year, Month month) {
        LocalDate lastDay = LocalDate.of(year, month, month.length(LocalDate.of(year, month, 1).isLeapYear()));
        while (lastDay.getDayOfWeek().getValue() != 4) { // 4 is Thursday
            lastDay = lastDay.minusDays(1);
        }
        return lastDay;
    }
    
    /**
     * Get the clean base symbol without any derivative information
     */
    public String getBaseSymbol() {
        return symbol;
    }
    
    /**
     * Check if this instrument is an index
     */
    public boolean isIndex() {
        return indexType != null && indexType != IndexType.UNKNOWN;
    }
    
    /**
     * Check if this instrument is a derivative (futures or options)
     */
    public boolean isDerivative() {
        return segment != null && segment.isDerivative();
    }
    
    /**
     * Get a formatted description of the instrument
     */
    public String getFormattedDescription() {
        StringBuilder sb = new StringBuilder();
        
        // Add index name if it's an index
        if (isIndex()) {
            sb.append(indexType.getDisplayName());
        } else {
            sb.append(symbol);
        }
        
        // Add derivative information
        if (isDerivative() && derivativeInfo != null) {
            if (segment == MarketSegment.EQUITY_OPTIONS || segment == MarketSegment.INDEX_OPTIONS) {
                sb.append(" ");
                sb.append(derivativeInfo.getIsCall() ? "CALL" : "PUT");
                if (derivativeInfo.getStrikePrice() != null) {
                    sb.append(" ").append(derivativeInfo.getStrikePrice());
                }
            } else if (segment == MarketSegment.EQUITY_FUTURES || segment == MarketSegment.INDEX_FUTURES) {
                sb.append(" FUT");
            }
            
            if (derivativeInfo.getExpiryDate() != null) {
                sb.append(" EXP: ").append(
                    derivativeInfo.getExpiryDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        }
        
        return sb.toString();
    }
}
