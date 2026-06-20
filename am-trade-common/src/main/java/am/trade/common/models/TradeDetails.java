package am.trade.common.models;

import am.trade.common.models.enums.TradePositionType;
import am.trade.common.models.enums.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Model class representing a complete trade within a portfolio
 * A complete trade can consist of multiple trade executions (entries and exits)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TradeModel> tradeExecutions;
    
    // Trade notes and tags
    private String notes;
    private List<String> tags;
    
    // User identification
    private String userId;
    
    // Trade analysis images (stored as Base64 strings or URLs to image storage)
    private List<Attachment> attachments;
    
    // Trade psychology and behavior data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TradePsychologyData psychologyData;
    
    // Trade entry reasoning (technical and fundamental analysis)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TradeEntryExistReasoning entryReasoning;

    // Trade exit reasoning (technical and fundamental analysis)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TradeEntryExistReasoning exitReasoning;

    public LocalDate getTradeDate() {
        return entryInfo.getTimestamp().toLocalDate();
    }

    public LocalDate getTradeEndDate() {
        return exitInfo != null ? exitInfo.getTimestamp().toLocalDate() : null;
    }
    

    @JsonIgnore
    public String getSymbol() {
        return instrumentInfo.getSymbol();
    }
    /**
     * Calculate profit/loss based on position type (LONG or SHORT)
     * For LONG positions: exitPrice - entryPrice
     * For SHORT positions: entryPrice - exitPrice
     * 
     * @return BigDecimal representing the profit/loss per unit
     */
    @JsonIgnore
    public BigDecimal calculateProfitLossPerUnit() {
        if (entryInfo == null || entryInfo.getPrice() == null || 
            exitInfo == null || exitInfo.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal entryPrice = entryInfo.getPrice();
        BigDecimal exitPrice = exitInfo.getPrice();
        
        if (TradePositionType.LONG.equals(tradePositionType)) {
            // For LONG: exit - entry
            return exitPrice.subtract(entryPrice);
        } else if (TradePositionType.SHORT.equals(tradePositionType)) {
            // For SHORT: entry - exit
            return entryPrice.subtract(exitPrice);
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Calculate total profit/loss for the trade
     * 
     * @return BigDecimal representing the total profit/loss
     */
    @JsonIgnore
    public BigDecimal calculateTotalProfitLoss() {
        if (entryInfo == null || entryInfo.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal profitLossPerUnit = calculateProfitLossPerUnit();
        return profitLossPerUnit.multiply(new BigDecimal(entryInfo.getQuantity()));
    }
    
    /**
     * Calculate holding time in hours
     * 
     * @return Long representing hours between entry and exit, or null if trade is still open
     */
    @JsonIgnore
    public Long calculateHoldingTimeHours() {
        if (entryInfo == null || entryInfo.getTimestamp() == null || 
            exitInfo == null || exitInfo.getTimestamp() == null) {
            return null;
        }
        
        return ChronoUnit.HOURS.between(entryInfo.getTimestamp(), exitInfo.getTimestamp());
    }
    
    /**
     * Calculate holding time in days
     * 
     * @return Long representing days between entry and exit, or null if trade is still open
     */
    @JsonIgnore
    public Long calculateHoldingTimeDays() {
        if (entryInfo == null || entryInfo.getTimestamp() == null || 
            exitInfo == null || exitInfo.getTimestamp() == null) {
            return null;
        }
        
        return ChronoUnit.DAYS.between(entryInfo.getTimestamp(), exitInfo.getTimestamp());
    }
    
    /**
     * Calculate profit/loss percentage
     * 
     * @return BigDecimal representing profit/loss as percentage
     */
    @JsonIgnore
    public BigDecimal calculateProfitLossPercentage() {
        if (entryInfo == null || entryInfo.getPrice() == null || 
            exitInfo == null || exitInfo.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal entryPrice = entryInfo.getPrice();
        BigDecimal exitPrice = exitInfo.getPrice();
        
        if (entryPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        if (TradePositionType.LONG.equals(tradePositionType)) {
            // For LONG: (exit - entry) / entry * 100
            return exitPrice.subtract(entryPrice)
                    .divide(entryPrice, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        } else if (TradePositionType.SHORT.equals(tradePositionType)) {
            // For SHORT: (entry - exit) / entry * 100
            return entryPrice.subtract(exitPrice)
                    .divide(entryPrice, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Check if this trade is profitable
     * 
     * @return true if profit/loss > 0, false otherwise
     */
    @JsonIgnore
    public boolean isProfitable() {
        return calculateProfitLossPerUnit().compareTo(BigDecimal.ZERO) > 0;
    }
}


