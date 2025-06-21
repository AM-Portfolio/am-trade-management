// package am.trade.kafka.service.metrics.impl;

// import am.trade.kafka.service.metrics.TradeMetricsService;
// import am.trade.kafka.service.metrics.calculator.*;
// import am.trade.models.document.Trade;
// import am.trade.models.document.TradeStatistics;
// import am.trade.models.document.statistics.*;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.stream.Collectors;

// /**
//  * Implementation of TradeMetricsService that orchestrates the calculation of all metrics
//  * using specialized calculator services
//  */
// @Service("kafkaTradeMetricsService")
// public class TradeMetricsServiceImpl implements TradeMetricsService {
    
//     private final TradeCountsCalculator tradeCountsCalculator;
//     private final PerformanceMetricsCalculator performanceMetricsCalculator;
//     private final ValueMetricsCalculator valueMetricsCalculator;
//     private final RiskMetricsCalculator riskMetricsCalculator;
//     private final TimeMetricsCalculator timeMetricsCalculator;
//     private final HoldingTimeMetricsCalculator holdingTimeMetricsCalculator;
//     private final AdditionalMetricsCalculator additionalMetricsCalculator;
//     private final StrategySpecificMetricsCalculator strategySpecificMetricsCalculator;
//     private final PortfolioTradeCountsCalculator portfolioTradeCountsCalculator;

//     @Autowired
//     public TradeMetricsServiceImpl(
//             TradeCountsCalculator tradeCountsCalculator,
//             PerformanceMetricsCalculator performanceMetricsCalculator,
//             ValueMetricsCalculator valueMetricsCalculator,
//             RiskMetricsCalculator riskMetricsCalculator,
//             TimeMetricsCalculator timeMetricsCalculator,
//             HoldingTimeMetricsCalculator holdingTimeMetricsCalculator,
//             AdditionalMetricsCalculator additionalMetricsCalculator,
//             StrategySpecificMetricsCalculator strategySpecificMetricsCalculator,
//             PortfolioTradeCountsCalculator portfolioTradeCountsCalculator) {
        
//         this.tradeCountsCalculator = tradeCountsCalculator;
//         this.performanceMetricsCalculator = performanceMetricsCalculator;
//         this.valueMetricsCalculator = valueMetricsCalculator;
//         this.riskMetricsCalculator = riskMetricsCalculator;
//         this.timeMetricsCalculator = timeMetricsCalculator;
//         this.holdingTimeMetricsCalculator = holdingTimeMetricsCalculator;
//         this.additionalMetricsCalculator = additionalMetricsCalculator;
//         this.strategySpecificMetricsCalculator = strategySpecificMetricsCalculator;
//         this.portfolioTradeCountsCalculator = portfolioTradeCountsCalculator;
//     }

//     @Override
//     public TradeStatistics calculateMetrics(List<Trade> trades) {
//         // Create a new TradeStatistics object or use an existing one
//         TradeStatistics statistics = new TradeStatistics();
        
//         // Calculate each group of metrics
//         statistics.setTradeCounts(tradeCountsCalculator.calculate(trades));
//         statistics.setPerformanceMetrics(performanceMetricsCalculator.calculate(trades));
//         statistics.setValueMetrics(valueMetricsCalculator.calculate(trades));
//         statistics.setRiskMetrics(riskMetricsCalculator.calculate(trades));
//         statistics.setTimeMetrics(timeMetricsCalculator.calculate(trades));
//         statistics.setHoldingTimeMetrics(holdingTimeMetricsCalculator.calculate(trades));
//         statistics.setAdditionalMetrics(additionalMetricsCalculator.calculate(trades));
//         statistics.setStrategySpecificMetrics(strategySpecificMetricsCalculator.calculate(trades));
        
//         // Calculate portfolio-level metrics if we have portfolio information in the trades
//         List<PortfolioTradeCounts> portfolioTradeCounts = portfolioTradeCountsCalculator.calculateForAllPortfolios(trades);
//         //statistics.setPortfolioTradeCounts(portfolioTradeCounts);
        
//         return statistics;
//     }

//     @Override
//     public TradeStatistics calculateMetricsForPortfolio(List<Trade> trades, String portfolioId) {
//         // Filter trades for the specific portfolio
//         List<Trade> portfolioTrades = trades.stream()
//                 .filter(trade -> portfolioId.equals(trade.getPortfolioId()))
//                 .collect(Collectors.toList());
        
//         // Calculate metrics for the filtered trades
//         return calculateMetrics(portfolioTrades);
//     }
// }
