package am.trade.models.document;

import am.trade.models.document.statistics.AdditionalMetrics;
import am.trade.models.document.statistics.HoldingTimeMetrics;
import am.trade.models.document.statistics.PerformanceMetrics;
import am.trade.models.document.statistics.RiskMetrics;
import am.trade.models.document.statistics.StrategySpecificMetrics;
import am.trade.models.document.statistics.TimeMetrics;
import am.trade.models.document.statistics.TradeCounts;
import am.trade.models.document.statistics.ValueMetrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

import am.trade.models.base.BaseDocument;

/**
 * MongoDB document for storing trade statistics
 * Contains aggregated information about trades including win/loss percentages
 * Organized into logical groups using nested classes for better maintainability
 * Includes portfolio-level trade counts and references to executed trades
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper=true)
@Document(collection = "trade_statistics")
@CompoundIndex(name = "portfolio_strategy_symbol_idx", def = "{'portfolio_id': 1, 'strategy_id': 1, 'symbol': 1}")
public class TradeStatistics extends BaseDocument {
    
    @Id
    private String id;

    @Indexed
    @Field("trader_id")
    private String traderId;

    @Indexed
    @Field("symbol")
    private String symbol;
    
    // Identifier fields
    @Indexed
    @Field("portfolio_id")
    private String portfolioId;
    
    @Indexed
    @Field("strategy_id")
    private String strategyId;
    
    // List of executed trades for this statistics record
    @DocumentReference(lazy = true)
    @Field("executed_trades")
    private List<Trade> executedTrades;
    
    // Grouped statistics
    @Field("trade_counts")
    private TradeCounts tradeCounts;
    
    @Field("performance")
    private PerformanceMetrics performanceMetrics;
    
    @Field("value")
    private ValueMetrics valueMetrics;
    
    @Field("risk")
    private RiskMetrics riskMetrics;
    
    @Field("time")
    private TimeMetrics timeMetrics;
    
    @Field("holding")
    private HoldingTimeMetrics holdingTimeMetrics;
    
    @Field("additional")
    private AdditionalMetrics additionalMetrics;
    
    @Field("strategy_specific")
    private StrategySpecificMetrics strategySpecificMetrics;
    
    // @Field("portfolio_trade_counts")
    // private List<PortfolioTradeCounts> portfolioTradeCounts;

    // @Field("trading_notes")
    // private List<TradingNote> tradingNotes;
}
