package am.trade.api.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearlyPerformance {
    private int year; // 2023, 2024
    private BigDecimal totalProfitLoss;
    private int tradeCount;
    private int winCount;
    private int lossCount;
    private double winRate;
    private BigDecimal averageWinAmount;
    private BigDecimal averageLossAmount;
    private double averageHoldingTime;
    private PerformanceMetrics metrics;
}
