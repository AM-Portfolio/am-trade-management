package am.trade.dashboard.service.metrics.grouping;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.common.models.enums.TradeBehaviorPattern;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Result object for trade grouping operations
 * Uses immutable design pattern with builder for construction
 */
@Getter
public class TradeGroupingResult {
    private final Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern;
    private final Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology;
    private final Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology;
    private final int fearBasedExitCount;
    private final int greedBasedEntryCount;
    private final int impulsiveTradeCount;
    private final int disciplinedTradeCount;
    
    /**
     * Constructor for TradeGroupingResult
     */
    public TradeGroupingResult(
            Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern,
            Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology,
            Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology,
            int fearBasedExitCount,
            int greedBasedEntryCount,
            int impulsiveTradeCount,
            int disciplinedTradeCount) {
        this.tradesByPattern = tradesByPattern;
        this.tradesByEntryPsychology = tradesByEntryPsychology;
        this.tradesByExitPsychology = tradesByExitPsychology;
        this.fearBasedExitCount = fearBasedExitCount;
        this.greedBasedEntryCount = greedBasedEntryCount;
        this.impulsiveTradeCount = impulsiveTradeCount;
        this.disciplinedTradeCount = disciplinedTradeCount;
    }
    
    /**
     * Create a builder for TradeGroupingResult
     * 
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for TradeGroupingResult
     * Follows the Builder Pattern for constructing complex objects
     */
    public static class Builder {
        private Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern;
        private Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology;
        private Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology;
        private int fearBasedExitCount;
        private int greedBasedEntryCount;
        private int impulsiveTradeCount;
        private int disciplinedTradeCount;
        
        public Builder tradesByPattern(Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
            this.tradesByPattern = tradesByPattern;
            return this;
        }
        
        public Builder tradesByEntryPsychology(Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology) {
            this.tradesByEntryPsychology = tradesByEntryPsychology;
            return this;
        }
        
        public Builder tradesByExitPsychology(Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology) {
            this.tradesByExitPsychology = tradesByExitPsychology;
            return this;
        }
        
        public Builder fearBasedExitCount(int fearBasedExitCount) {
            this.fearBasedExitCount = fearBasedExitCount;
            return this;
        }
        
        public Builder greedBasedEntryCount(int greedBasedEntryCount) {
            this.greedBasedEntryCount = greedBasedEntryCount;
            return this;
        }
        
        public Builder impulsiveTradeCount(int impulsiveTradeCount) {
            this.impulsiveTradeCount = impulsiveTradeCount;
            return this;
        }
        
        public Builder disciplinedTradeCount(int disciplinedTradeCount) {
            this.disciplinedTradeCount = disciplinedTradeCount;
            return this;
        }
        
        public TradeGroupingResult build() {
            return new TradeGroupingResult(
                    tradesByPattern,
                    tradesByEntryPsychology,
                    tradesByExitPsychology,
                    fearBasedExitCount,
                    greedBasedEntryCount,
                    impulsiveTradeCount,
                    disciplinedTradeCount);
        }
    }
}
