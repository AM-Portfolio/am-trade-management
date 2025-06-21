// package am.trade.kafka.service.consumer;

// import am.trade.kafka.service.metrics.TradeMetricsService;
// import am.trade.models.document.Portfolio;
// import am.trade.models.document.Trade;
// import am.trade.models.document.TradeStatistics;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// /**
//  * Kafka consumer service for processing trade events and calculating metrics
//  */
// @Service
// @Slf4j
// public class TradeMetricsConsumerService {

//     private final TradeMetricsService tradeMetricsService;
    
//     @Autowired
//     public TradeMetricsConsumerService(TradeMetricsService tradeMetricsService) {
//         this.tradeMetricsService = tradeMetricsService;
//     }
    
//     /**
//      * Processes incoming trade events from Kafka and calculates metrics
//      * 
//      * @param trades List of trades to process
//      */
//     @KafkaListener(
//         topics = "${kafka.topics.trades}",
//         groupId = "${kafka.consumer.group-id}",
//         containerFactory = "tradeKafkaListenerContainerFactory"
//     )
//     public void consumeTrades(List<Trade> trades) {
//         log.info("Received batch of {} trades for metrics calculation", trades.size());
        
//         try {
//             // Calculate overall trade statistics
//             TradeStatistics statistics = tradeMetricsService.calculateMetrics(trades);
            
//             // Process and save statistics (implementation would depend on your repository layer)
//             log.info("Calculated metrics for {} trades", trades.size());
            
//             // Group trades by portfolio for portfolio-level metrics
//             processPortfolioMetrics(trades);
            
//         } catch (Exception e) {
//             log.error("Error processing trade metrics: {}", e.getMessage(), e);
//         }
//     }
    
//     /**
//      * Process portfolio-level metrics from trades
//      * 
//      * @param trades List of trades to process
//      */
//     private void processPortfolioMetrics(List<Trade> trades) {
//         // Group trades by portfolio
//         Map<String, List<Trade>> tradesByPortfolio = trades.stream()
//                 .filter(trade -> trade.getPortfolioId() != null)
//                 .collect(Collectors.groupingBy(Trade::getPortfolioId));
        
//         // Process each portfolio
//         for (Map.Entry<String, List<Trade>> entry : tradesByPortfolio.entrySet()) {
//             String portfolioId = entry.getKey();
//             List<Trade> portfolioTrades = entry.getValue();
            
//             try {
//                 // Calculate portfolio-specific metrics
//                 TradeStatistics portfolioStatistics = tradeMetricsService.calculateMetricsForPortfolio(
//                         portfolioTrades, portfolioId);
                
//                 // Here you would update the portfolio document with the new metrics
//                 // This would typically involve a repository call
//                 log.info("Calculated metrics for portfolio {} with {} trades", 
//                         portfolioId, portfolioTrades.size());
                
//             } catch (Exception e) {
//                 log.error("Error processing metrics for portfolio {}: {}", 
//                         portfolioId, e.getMessage(), e);
//             }
//         }
//     }
// }
