package am.trade.dashboard.service.metrics;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradingFeedback;
import am.trade.dashboard.exception.FeedbackGenerationException;
import am.trade.dashboard.service.metrics.feedback.TradingFeedbackFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for generating personalized trading feedback based on trade psychology and entry/exit reasoning
 * This service delegates to the TradingFeedbackFacade which orchestrates the analysis process
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TradingFeedbackService {
    
    private final TradingFeedbackFacade tradingFeedbackFacade;
    
    /**
     * Generate trading feedback based on trade details
     * 
     * @param trades List of trade details
     * @return TradingFeedback object with personalized insights
     */
    public TradingFeedback generateFeedback(List<TradeDetails> trades) {
        log.info("Generating trading feedback for {} trades", trades != null ? trades.size() : 0);
        
        try {
            if (trades == null || trades.isEmpty()) {
                log.warn("No trades provided for feedback generation");
                return new TradingFeedback();
            }
            
            return tradingFeedbackFacade.generateFeedback(trades);
        } catch (FeedbackGenerationException e) {
            log.error("Error generating trading feedback", e);
            return new TradingFeedback(); // Return empty feedback object in case of error
        } catch (Exception e) {
            log.error("Unexpected error during feedback generation", e);
            return new TradingFeedback(); // Return empty feedback object in case of error
        }
    }
}
