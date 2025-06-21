// package am.trade.kafka.service.metrics.calculator.impl;

// import am.trade.kafka.service.metrics.calculator.TradeCountsCalculator;
// import am.trade.models.document.Trade;
// import am.trade.models.document.statistics.TradeCounts;
// import org.springframework.stereotype.Service;

// import java.util.List;

// /**
//  * Implementation of TradeCountsCalculator that calculates trade count metrics
//  */
// @Service
// public class TradeCountsCalculatorImpl implements TradeCountsCalculator {

//     @Override
//     public TradeCounts calculate(List<Trade> trades) {
//         if (trades == null || trades.isEmpty()) {
//             return TradeCounts.builder()
//                     .total(0)
//                     .winning(0)
//                     .losing(0)
//                     .breakEven(0)
//                     .build();
//         }

//         int total = trades.size();
//         int winning = 0;
//         int losing = 0;
//         int breakEven = 0;

//         for (Trade trade : trades) {
//             // Determine if the trade is winning, losing, or break-even
//             // This logic will depend on your Trade model structure
//             // Access profit/loss from metrics or calculate it based on available data
//             BigDecimal profitLoss = calculateProfitLoss(trade);
//             if (profitLoss == null) {
//                 continue; // Skip trades without profit/loss information
//             }
            
//             if (profitLoss.compareTo(BigDecimal.ZERO) > 0) {
//                 winning++;
//             } else if (profitLoss.compareTo(BigDecimal.ZERO) < 0) {
//             } else if (comparison < 0) {
//                 losing++;
//             } else {
//                 breakEven++;
//             }
//         }

//         return TradeCounts.builder()
//                 .total(total)
//                 .winning(winning)
//                 .losing(losing)
//                 .breakEven(breakEven)
//                 .build();
//     }
// }
