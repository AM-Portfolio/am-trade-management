package am.trade.services.service.impl;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeEntryReasoning;
import am.trade.common.models.TradePsychologyData;
import am.trade.common.models.enums.FundamentalEntryReason;
import am.trade.common.models.enums.TechnicalEntryReason;
import am.trade.common.models.enums.TradeBehaviorPattern;
import am.trade.persistence.entity.TradeDetailsEntity;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.persistence.repository.TradeDetailsRepository;
import am.trade.services.service.TradeDetailsService;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for analyzing trade psychology and entry reasoning
 */
@Service
public class TradeAnalysisService {

    private final TradeDetailsService tradeDetailsService;
    private final TradeDetailsMapper tradeDetailsMapper;

    public TradeAnalysisService(TradeDetailsService tradeDetailsService, TradeDetailsMapper tradeDetailsMapper) {
        this.tradeDetailsService = tradeDetailsService;
        this.tradeDetailsMapper = tradeDetailsMapper;
    }

    /**
     * Add psychology data and entry reasoning to an existing trade
     * 
     * @param tradeId The ID of the trade to update
     * @param psychologyData The psychology data to add
     * @param entryReasoning The entry reasoning to add
     * @return The updated trade details
     */
    public TradeDetails addTradeAnalysis(String tradeId, TradePsychologyData psychologyData, TradeEntryReasoning entryReasoning) {
        Optional<TradeDetails> tradeDetailsOpt = tradeDetailsService.findModelByTradeId(tradeId);
        
        if (tradeDetailsOpt.isPresent()) {
            TradeDetails tradeDetails = tradeDetailsOpt.get();
            tradeDetails.setPsychologyData(psychologyData);
            tradeDetails.setEntryReasoning(entryReasoning);
            
            TradeDetails savedTradeDetails = tradeDetailsService.saveTradeDetails(tradeDetails);
            return savedTradeDetails;
        }
        
        throw new RuntimeException("Trade not found with ID: " + tradeId);
    }
    
    /**
     * Create a sample psychology data object with common patterns
     * 
     * @return A sample psychology data object
     */
    public TradePsychologyData createSamplePsychologyData() {
        TradePsychologyData data = new TradePsychologyData();
        
        // Add entry psychology factors
        data.addEntryPsychology("FEAR_OF_MISSING_OUT", null);
        data.addEntryPsychology("FOLLOWING_THE_PLAN", null);
        data.addEntryPsychology("CUSTOM_FACTOR", "Confidence from recent success");
        
        // Add exit psychology factors
        data.addExitPsychology("DISCIPLINE", null);
        data.addExitPsychology("TAKING_PROFITS", null);
        
        // Add behavior patterns
        data.addBehaviorPattern("POSITION_SIZING_ISSUES", null);
        data.addBehaviorPattern("CUSTOM_PATTERN", "Entered too early before confirmation");
        
        // Add categorized tags
        data.addCategorizedTag("STRATEGY", null, "Breakout");
        data.addCategorizedTag("RISK_LEVEL", null, "Medium");
        data.addCategorizedTag("CUSTOM_CATEGORY", "Personal Factors", "Well Rested");
        
        // Add psychology notes
        data.setPsychologyNotes("Felt confident due to previous trade success, but entered slightly early due to FOMO.");
        
        return data;
    }
    
    /**
     * Create a sample entry reasoning object with technical and fundamental factors
     * 
     * @return A sample entry reasoning object
     */
    public TradeEntryReasoning createSampleEntryReasoning() {
        TradeEntryReasoning reasoning = new TradeEntryReasoning();
        
        // Add technical reasons
        reasoning.addTechnicalReason("RESISTANCE_BREAKOUT", null);
        reasoning.addTechnicalReason("VOLUME_SPIKE", null);
        reasoning.addTechnicalReason("CUSTOM_TECHNICAL", "Golden cross on 4-hour chart");
        
        // Add fundamental reasons
        reasoning.addFundamentalReason("EARNINGS_BEAT", null);
        reasoning.addFundamentalReason("CUSTOM_FUNDAMENTAL", "New product announcement expected");
        
        // Set primary reason
        reasoning.setPrimaryReason("RESISTANCE_BREAKOUT");
        
        // Set confidence level
        reasoning.setConfidenceLevel(8);
        
        // Add supporting indicators
        reasoning.addSupportingIndicator("RSI showing bullish divergence");
        reasoning.addSupportingIndicator("Increasing volume on breakout");
        
        // Add conflicting indicators
        reasoning.addConflictingIndicator("Market sentiment bearish");
        
        // Set reasoning summary
        reasoning.setReasoningSummary("Entered long position after price broke above key resistance with increasing volume. " +
                "Strong earnings report provided fundamental support. RSI showed bullish divergence confirming entry. " +
                "Main concern was overall bearish market sentiment, but stock-specific factors outweighed this.");
        
        return reasoning;
    }
    
    /**
     * Analyze common patterns across multiple trades for portfolios
     * 
     * @param portfolioIds List of portfolio IDs to analyze trades for
     * @return A map of behavior patterns and their frequency
     */
    public Map<TradeBehaviorPattern, Integer> analyzeBehaviorPatterns(List<String> portfolioIds) {
        List<TradeDetails> portfolioTrades = tradeDetailsService.findByPortfolioIdIn(portfolioIds);
        Map<TradeBehaviorPattern, Integer> patternFrequency = new HashMap<>();
        
        for (TradeDetails trade : portfolioTrades) {
            if (trade.getPsychologyData() != null && trade.getPsychologyData().getBehaviorPatterns() != null) {
                for (TradeBehaviorPattern pattern : trade.getPsychologyData().getBehaviorPatterns()) {
                    patternFrequency.put(pattern, patternFrequency.getOrDefault(pattern, 0) + 1);
                }
            }
        }
        
        return patternFrequency;
    }
    
    /**
     * Find trades with specific technical or fundamental reasons
     * 
     * @param portfolioIds List of portfolio IDs to search trades for
     * @param technicalReason The technical reason to search for (can be null)
     * @param fundamentalReason The fundamental reason to search for (can be null)
     * @return A list of matching trade details
     */
    public List<TradeDetails> findTradesByEntryReason(List<String> portfolioIds, TechnicalEntryReason technicalReason, 
                                                     FundamentalEntryReason fundamentalReason) {
        List<TradeDetails> portfolioTrades = tradeDetailsService.findByPortfolioIdIn(portfolioIds);
        List<TradeDetails> matchingTrades = new ArrayList<>();
        
        for (TradeDetails trade : portfolioTrades) {
            if (trade.getEntryReasoning() != null) {
                boolean matches = false;
                
                if (technicalReason != null && trade.getEntryReasoning().getTechnicalReasons() != null) {
                    matches = trade.getEntryReasoning().getTechnicalReasons().contains(technicalReason);
                }
                
                if (!matches && fundamentalReason != null && trade.getEntryReasoning().getFundamentalReasons() != null) {
                    matches = trade.getEntryReasoning().getFundamentalReasons().contains(fundamentalReason);
                }
                
                if (matches) {
                    matchingTrades.add(trade);
                }
            }
        }
        
        return matchingTrades;
    }
}
