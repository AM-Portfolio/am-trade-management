package am.trade.common.models;

import am.trade.common.models.enums.TechnicalEntryReason;
import am.trade.common.models.enums.FundamentalEntryReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for storing technical and fundamental reasons for entering a trade
 * Includes a summary field for detailed explanation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeEntryReasoning {
    // Technical analysis reasons for entering the trade
    private List<TechnicalEntryReason> technicalReasons;
    
    // Fundamental analysis reasons for entering the trade
    private List<FundamentalEntryReason> fundamentalReasons;
    
    // Primary reason for entry (can be technical or fundamental)
    private String primaryReason;
    
    // Detailed summary of the entry reasoning
    private String reasoningSummary;
    
    // Confidence level in the analysis (1-10)
    private Integer confidenceLevel;
    
    // Supporting indicators or metrics
    private List<String> supportingIndicators;
    
    // Conflicting indicators or metrics
    private List<String> conflictingIndicators;
    
    /**
     * Add a technical reason for entering the trade
     * 
     * @param code The code for the technical reason
     * @param description Optional description for new custom values
     * @return This instance for method chaining
     */
    public TradeEntryReasoning addTechnicalReason(String code, String description) {
        if (technicalReasons == null) {
            technicalReasons = new ArrayList<>();
        }
        technicalReasons.add(TechnicalEntryReason.fromCode(code, description));
        return this;
    }
    
    /**
     * Add a fundamental reason for entering the trade
     * 
     * @param code The code for the fundamental reason
     * @param description Optional description for new custom values
     * @return This instance for method chaining
     */
    public TradeEntryReasoning addFundamentalReason(String code, String description) {
        if (fundamentalReasons == null) {
            fundamentalReasons = new ArrayList<>();
        }
        fundamentalReasons.add(FundamentalEntryReason.fromCode(code, description));
        return this;
    }
    
    /**
     * Add a supporting indicator
     * 
     * @param indicator The supporting indicator or metric
     * @return This instance for method chaining
     */
    public TradeEntryReasoning addSupportingIndicator(String indicator) {
        if (supportingIndicators == null) {
            supportingIndicators = new ArrayList<>();
        }
        supportingIndicators.add(indicator);
        return this;
    }
    
    /**
     * Add a conflicting indicator
     * 
     * @param indicator The conflicting indicator or metric
     * @return This instance for method chaining
     */
    public TradeEntryReasoning addConflictingIndicator(String indicator) {
        if (conflictingIndicators == null) {
            conflictingIndicators = new ArrayList<>();
        }
        conflictingIndicators.add(indicator);
        return this;
    }
}
