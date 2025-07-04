package am.trade.common.models;

import am.trade.common.models.enums.TradePositionType;
import am.trade.common.models.enums.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private InstrumentInfo instrumentInfo;
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
    
    // User identification
    private String userId;
    
    // Trade analysis images (stored as Base64 strings or URLs to image storage)
    private List<Attachment> attachments;
    
    // Trade psychology and behavior data
    private TradePsychologyData psychologyData;
    
    // Trade entry reasoning (technical and fundamental analysis)
    private TradeEntryReasoning entryReasoning;


    public LocalDate getTradeDate() {
        return entryInfo.getTimestamp().toLocalDate();
    }

    public LocalDate getTradeEndDate() {
        return exitInfo != null ? exitInfo.getTimestamp().toLocalDate() : null;
    }
}


