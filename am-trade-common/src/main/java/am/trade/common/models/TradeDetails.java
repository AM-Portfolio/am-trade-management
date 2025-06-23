package am.trade.common.models;

import am.trade.common.models.enums.TradePositionType;
import am.trade.common.models.enums.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Model class representing a complete trade within a portfolio
 * A complete trade can consist of multiple trade executions (entries and exits)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeDetails {
    private String tradeId;
    private String portfolioId;
    private String symbol;
    private String strategy;
    
    // Trade status (WIN, LOSS, OPEN, BREAK_EVEN)
    private TradeStatus status;
    
    // Trade position type (LONG, SHORT)
    private TradePositionType tradePositionType;
    
    // Entry and exit details
    private EntryExitInfo entryInfo;
    private EntryExitInfo exitInfo;
    
    // Trade metrics
    private TradeMetrics metrics;
    
    // Associated trade executions
    private List<TradeModel> tradeExecutions;
    
    // Trade notes and tags
    private String notes;
    private List<String> tags;
}
