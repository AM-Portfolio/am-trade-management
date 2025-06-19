package am.trade.dashboard.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for trade metrics data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeMetrics {
    
    private LocalDateTime timestamp;
    
    // Trade volume metrics
    private long totalTradeCount;
    private BigDecimal totalTradeValue;
    private BigDecimal averageTradeSize;
    private BigDecimal largestTradeValue;
    private BigDecimal smallestTradeValue;
    
    // Trade type breakdown
    private Map<String, Long> tradeCountByType;
    private Map<String, BigDecimal> tradeValueByType;
    
    // Symbol metrics
    private Map<String, Long> tradeCountBySymbol;
    private Map<String, BigDecimal> tradeValueBySymbol;
    private String mostTradedSymbol;
    
    // Portfolio metrics
    private Map<String, Long> tradeCountByPortfolio;
    private Map<String, BigDecimal> tradeValueByPortfolio;
    private String topPerformingPortfolio;
    
    // Trader metrics
    private Map<String, Long> tradeCountByTrader;
    private Map<String, BigDecimal> tradeValueByTrader;
    private String mostActiveTrader;
    
    // Time-based metrics
    private Map<String, Long> tradeCountByHour;
    private Map<String, BigDecimal> tradeValueByHour;
    private String peakTradingHour;
    
    // Performance metrics
    private BigDecimal totalCommissions;
    private BigDecimal totalFees;
    private BigDecimal netTradeValue;
    private BigDecimal profitLossPercentage;
}
