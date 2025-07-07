package am.trade.common.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Basic trade summary model containing essential information and metrics
 * for quick access and overview. This model is designed to be stored in MongoDB
 * and retrieved frequently without the overhead of detailed metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trade_summary_basic")
public class TradeSummaryBasic {

    @Id
    private String id;
    private String userId;
    private String name;
    private String description;
    private String ownerId;
    private boolean active;
    private String currency;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;
    
    // Basic trade metrics for quick overview
    private PortfolioMetrics basicMetrics;
    
    // List of portfolios this trade summary belongs to
    private List<String> portfolioIds;

    private List<TradeDetails> tradeDetails;
    
    // Basic trade counts
    private int totalTradeCount;
    private int winningTradeCount;
    private int losingTradeCount;
    
    // Legacy performance statistics (maintained for backward compatibility and quick access)
    private BigDecimal totalProfitLoss;
    private BigDecimal winRate;
    private BigDecimal averageHoldingTime;
    private BigDecimal averagePositionSize;
    
    // Reference to detailed metrics document
    private String detailedMetricsId;
}
