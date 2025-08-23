package am.trade.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing common time periods for filtering trade metrics
 */
@Getter
@AllArgsConstructor
public enum TimePeriodFilter {
    TODAY("Today", 0, 0),
    YESTERDAY("Yesterday", 1, 1),
    LAST_7_DAYS("Last 7 Days", 7, 0),
    LAST_14_DAYS("Last 14 Days", 14, 0),
    LAST_30_DAYS("Last 30 Days", 30, 0),
    THIS_WEEK("This Week", 0, 0),
    LAST_WEEK("Last Week", 7, 7),
    THIS_MONTH("This Month", 0, 0),
    LAST_MONTH("Last Month", 0, 0),
    THIS_QUARTER("This Quarter", 0, 0),
    LAST_QUARTER("Last Quarter", 0, 0),
    THIS_YEAR("This Year", 0, 0),
    LAST_YEAR("Last Year", 0, 0),
    LAST_3_MONTHS("Last 3 Months", 90, 0),
    LAST_6_MONTHS("Last 6 Months", 180, 0),
    LAST_12_MONTHS("Last 12 Months", 365, 0),
    LAST_2_YEARS("Last 2 Years", 730, 0),
    LAST_3_YEARS("Last 3 Years", 1095, 0),
    LAST_5_YEARS("Last 5 Years", 1825, 0),
    CUSTOM("Custom Date Range", 0, 0);

    private final String displayName;
    private final int daysBack;
    private final int daysOffset;
}
