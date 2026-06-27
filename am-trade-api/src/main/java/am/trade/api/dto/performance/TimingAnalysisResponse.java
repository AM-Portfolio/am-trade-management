package am.trade.api.dto.performance;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Timing analysis response – matches Flutter TimingAnalysisDto.
 * Contains breakdowns by hour, day-of-week, month, year, and week.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimingAnalysisResponse {

    private List<HourlyPerformance> hourlyPerformance;
    private List<DayOfWeekPerformance> dayOfWeekPerformance;
    private List<MonthlyPerformance> monthlyPerformance;
    private List<YearlyPerformance> yearlyPerformance;
    private List<WeeklyPerformance> weeklyPerformance;

    private Integer bestTradingHour;
    private Integer worstTradingHour;
    private String bestTradingDay;
    private String worstTradingDay;
    private String bestTradingMonth;
    private String worstTradingMonth;

    /* ── Nested classes matching Flutter DTOs ───────────────────────────── */

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HourlyPerformance {
        private int hour;
        private int tradeCount;
        private int winCount;
        private int lossCount;
        private double winRate;
        private double totalProfitLoss;
        private double averageWinAmount;
        private double averageLossAmount;
        private double averageHoldingTime;
        private Map<String, Object> metrics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DayOfWeekPerformance {
        private String dayOfWeek;
        private int dayOrder;
        private int tradeCount;
        private int winCount;
        private int lossCount;
        private double winRate;
        private double totalProfitLoss;
        private double averageWinAmount;
        private double averageLossAmount;
        private double averageHoldingTime;
        private Map<String, Object> metrics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MonthlyPerformance {
        private String month;
        private int monthOrder;
        private int tradeCount;
        private int winCount;
        private int lossCount;
        private double winRate;
        private double totalProfitLoss;
        private double averageWinAmount;
        private double averageLossAmount;
        private double averageHoldingTime;
        private Map<String, Object> metrics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class YearlyPerformance {
        private int year;
        private int tradeCount;
        private int winCount;
        private int lossCount;
        private double winRate;
        private double totalProfitLoss;
        private double averageWinAmount;
        private double averageLossAmount;
        private double averageHoldingTime;
        private Map<String, Object> metrics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WeeklyPerformance {
        /** Format: "2024-W01" */
        private String weekId;
        private int tradeCount;
        private int winCount;
        private int lossCount;
        private double winRate;
        private double totalProfitLoss;
        private Map<String, Object> metrics;
    }
}
