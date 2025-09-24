package am.trade.dashboard.service.metrics.feedback;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradingFeedback;
import am.trade.dashboard.exception.FeedbackGenerationException;
import am.trade.dashboard.model.feeback.DecisionAnalysis;
import am.trade.dashboard.model.feeback.PatternAnalysis;
import am.trade.dashboard.model.feeback.PsychologyAnalysis;
import am.trade.dashboard.model.feeback.RiskManagementAnalysis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Facade service that coordinates the generation of trading feedback
 * by delegating to specialized analyzers.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TradingFeedbackFacade {
    
    private final PsychologyAnalyzer psychologyAnalyzer;
    private final PatternAnalyzer patternAnalyzer;
    private final DecisionAnalyzer decisionAnalyzer;
    private final RiskManagementAnalyzer riskAnalyzer;
    
    /**
     * Generate comprehensive trading feedback based on trade details
     * 
     * @param trades List of trade details
     * @return TradingFeedback object with personalized insights
     */
    public TradingFeedback generateFeedback(List<TradeDetails> trades) {
        log.info("Generating trading feedback for {} trades", trades != null ? trades.size() : 0);
        
        if (trades == null || trades.isEmpty()) {
            log.warn("No trades provided for feedback generation");
            return new TradingFeedback();
        }
        
        try {
            // Delegate to specialized analyzers
            PsychologyAnalysis psychologyAnalysis = psychologyAnalyzer.analyze(trades);
            PatternAnalysis patternAnalysis = patternAnalyzer.analyze(trades);
            DecisionAnalysis decisionAnalysis = decisionAnalyzer.analyze(trades);
            RiskManagementAnalysis riskAnalysis = riskAnalyzer.analyze(trades);
            
            // Build and return the feedback object
            TradingFeedback feedback = TradingFeedback.builder()
                    .strengthAreas(psychologyAnalysis.getStrengthAreas())
                    .improvementAreas(psychologyAnalysis.getImprovementAreas())
                    .psychologyInsights(psychologyAnalysis.getPsychologyInsights())
                    .emotionalPatternObservations(psychologyAnalysis.getEmotionalPatternObservations())
                    .recommendedBehaviorChanges(patternAnalysis.getRecommendedBehaviorChanges())
                    .tradingHabitsToReinforce(patternAnalysis.getPositiveHabits())
                    .patternSpecificFeedback(patternAnalysis.getPatternSpecificFeedback())
                    .successPatternRecommendations(patternAnalysis.getSuccessPatternRecommendations())
                    .entryDecisionFeedback(decisionAnalysis.getEntryDecisionFeedback())
                    .exitDecisionFeedback(decisionAnalysis.getExitDecisionFeedback())
                    .overallDecisionQualityFeedback(decisionAnalysis.getOverallDecisionQualityFeedback())
                    .riskManagementFeedback(riskAnalysis.getRiskManagementFeedback())
                    .riskManagementSuggestions(riskAnalysis.getRiskManagementSuggestions())
                    .build();
            
            log.info("Successfully generated trading feedback");
            return feedback;
            
        } catch (Exception e) {
            log.error("Error generating trading feedback", e);
            throw new FeedbackGenerationException("Failed to generate trading feedback", e);
        }
    }
}
