package am.trade.api.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimingAnalysis {
    private List<HourlyPerformance> hourlyPerformance;
    private List<DayOfWeekPerformance> dayOfWeekPerformance;
    private List<MonthlyPerformance> monthlyPerformance;
    private List<YearlyPerformance> yearlyPerformance;
    private List<WeeklyPerformance> weeklyPerformance;

    // Best/Worst summaries
    private Integer bestTradingHour;
    private Integer worstTradingHour;
    private String bestTradingDay;
    private String worstTradingDay;
    private String bestTradingMonth;
    private String worstTradingMonth;
}
