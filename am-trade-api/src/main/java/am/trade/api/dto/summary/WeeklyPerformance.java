package am.trade.api.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyPerformance {
    private String weekId; // e.g. "2023-W10"
    private LocalDate startDate;
    private LocalDate endDate;
    private PerformanceMetrics metrics;
}
