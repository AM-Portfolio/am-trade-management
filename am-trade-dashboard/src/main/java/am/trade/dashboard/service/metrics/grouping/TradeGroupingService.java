package am.trade.dashboard.service.metrics.grouping;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.common.models.enums.TradeBehaviorPattern;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for grouping trades by various factors
 */
@Service
public class TradeGroupingService {

    /**
     * Group trades by behavior patterns and psychology factors
     * 
     * @param trades List of trades to group
     * @return Result containing grouped trades and counts
     */
    public TradeGroupingResult groupTradesByFactors(List<TradeDetails> trades) {
        Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern = new HashMap<>();
        Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology = new HashMap<>();
        Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology = new HashMap<>();
        
        // Counters for psychology factors
        int fearBasedExitCount = 0;
        int greedBasedEntryCount = 0;
        int impulsiveTradeCount = 0;
        int disciplinedTradeCount = 0;
        
        // Process each trade
        for (TradeDetails trade : trades) {
            if (trade.getPsychologyData() == null) {
                continue;
            }
            
            // Group by behavior pattern
            if (trade.getPsychologyData().getBehaviorPatterns() != null) {
                for (TradeBehaviorPattern pattern : trade.getPsychologyData().getBehaviorPatterns()) {
                    tradesByPattern.computeIfAbsent(pattern, k -> new ArrayList<>()).add(trade);
                }
            }
            
            // Group by entry psychology factors
            if (trade.getPsychologyData().getEntryPsychologyFactors() != null) {
                for (EntryPsychology factor : trade.getPsychologyData().getEntryPsychologyFactors()) {
                    tradesByEntryPsychology.computeIfAbsent(factor, k -> new ArrayList<>()).add(trade);
                    
                    // Count specific psychology factors for emotional control metrics
                    if (factor.equals(EntryPsychology.OVERCONFIDENCE) || 
                        factor.equals(EntryPsychology.FEAR_OF_MISSING_OUT)) {
                        greedBasedEntryCount++;
                    }
                    
                    if (!factor.equals(EntryPsychology.FOLLOWING_THE_PLAN)) {
                        impulsiveTradeCount++;
                    }
                }
            }
            
            // Group by exit psychology factors
            if (trade.getPsychologyData().getExitPsychologyFactors() != null) {
                for (ExitPsychology factor : trade.getPsychologyData().getExitPsychologyFactors()) {
                    tradesByExitPsychology.computeIfAbsent(factor, k -> new ArrayList<>()).add(trade);
                    
                    // Count specific psychology factors for emotional control metrics
                    if (factor.equals(ExitPsychology.FEAR) || 
                        factor.equals(ExitPsychology.PANIC)) {
                        fearBasedExitCount++;
                    }
                    
                    if (factor.equals(ExitPsychology.DISCIPLINE) || 
                        factor.equals(ExitPsychology.TAKING_PROFITS) || 
                        factor.equals(ExitPsychology.CUTTING_LOSSES)) {
                        disciplinedTradeCount++;
                    }
                }
            }
        }
        
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
