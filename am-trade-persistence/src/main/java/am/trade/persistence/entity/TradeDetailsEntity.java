package am.trade.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.TradeMetrics;
import am.trade.common.models.TradeModel;
import am.trade.common.models.enums.TradePositionType;
import am.trade.common.models.enums.TradeStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MongoDB embedded document for Trade Details
 * This is embedded within the PortfolioEntity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trade_details")
public class TradeDetailsEntity {
    
    private String tradeId;
    
    @Indexed
    private String portfolioId;
    
    @Indexed
    private String symbol;
    
    private TradePositionType tradePositionType;
    private TradeStatus status;
    
    // Entry and exit information
    private EntryExitInfo entryInfo;
    private EntryExitInfo exitInfo;
    
    // Trade metrics
    private TradeMetrics metrics;
    
    // List of trade executions that make up this trade
    private List<TradeModel> tradeExecutions;
}
