// package am.trade.kafka.service.metrics.calculator.impl;

// import am.trade.kafka.service.metrics.calculator.PerformanceMetricsCalculator;
// import am.trade.kafka.service.metrics.calculator.TradeCountsCalculator;
// import am.trade.models.document.Trade;
// import am.trade.models.document.statistics.PerformanceMetrics;
// import am.trade.models.document.statistics.TradeCounts;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.math.BigDecimal;
// import java.math.RoundingMode;
// import java.util.List;

// /**
//  * Implementation of PerformanceMetricsCalculator that calculates performance metrics
//  */
// @Service
// public class PerformanceMetricsCalculatorImpl implements PerformanceMetricsCalculator {

//     private final TradeCountsCalculator tradeCountsCalculator;
    
//     @Autowired
//     public PerformanceMetricsCalculatorImpl(TradeCountsCalculator tradeCountsCalculator) {
//         this.tradeCountsCalculator = tradeCountsCalculator;
//     }

//     @Override
//     public PerformanceMetrics calculate(List<Trade> trades) {
//         if (trades == null || trades.isEmpty()) {
//             return PerformanceMetrics.builder()
//                     .winPercentage(BigDecimal.ZERO)
//                     .lossPercentage(BigDecimal.ZERO)
//                     .breakEvenPercentage(BigDecimal.ZERO)
//                     .build();
//         }

//         // Get trade counts first
//         TradeCounts counts = tradeCountsCalculator.calculate(trades);
        
//         // Calculate percentages with proper rounding
//         BigDecimal total = BigDecimal.valueOf(counts.getTotal());
        
//         BigDecimal winPercentage = total.compareTo(BigDecimal.ZERO) > 0 
//                 ? BigDecimal.valueOf(counts.getWinning()).divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
//                 : BigDecimal.ZERO;
                
//         BigDecimal lossPercentage = total.compareTo(BigDecimal.ZERO) > 0 
//                 ? BigDecimal.valueOf(counts.getLosing()).divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
//                 : BigDecimal.ZERO;
                
//         BigDecimal breakEvenPercentage = total.compareTo(BigDecimal.ZERO) > 0 
//                 ? BigDecimal.valueOf(counts.getBreakEven()).divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
//                 : BigDecimal.ZERO;

//         return PerformanceMetrics.builder()
//                 .winPercentage(winPercentage)
//                 .lossPercentage(lossPercentage)
//                 .breakEvenPercentage(breakEvenPercentage)
//                 .build();
//     }
// }
