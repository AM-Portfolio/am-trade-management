package am.trade.common.models;

import am.trade.common.models.enums.TechnicalEntryReason;
import am.trade.common.models.enums.FundamentalEntryReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Model class for storing technical and fundamental reasons for entering and exiting a trade
 * Includes summary fields for detailed explanation of both entry and exit decisions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TradeEntryExistReasoning {
    // Technical analysis reasons for entering the trade
    private List<TechnicalEntryReason> technicalReasons;
    
    // Fundamental analysis reasons for entering the trade
    private List<FundamentalEntryReason> fundamentalReasons;
    
    // Primary reason for entry (can be technical or fundamental)
    private String primaryReason;
    
    // Detailed summary of the entry reasoning
    private String reasoningSummary;
    
    // Confidence level in the entry analysis (1-10)
    private Integer confidenceLevel;
    
    // Supporting indicators or metrics for entry
    private List<String> supportingIndicators;
    
    // Conflicting indicators or metrics for entry
    private List<String> conflictingIndicators;
    
    // Primary reason for exit (can be technical or fundamental)
    private String exitPrimaryReason;
    
    // Detailed summary of the exit reasoning
    private String exitReasoningSummary;
    
    // Confidence level in the exit analysis (1-10)
    private Integer exitConfidenceLevel;
    
    // Supporting indicators or metrics for exit
    private List<String> exitSupportingIndicators;
    
    // Conflicting indicators or metrics for exit
    private List<String> exitConflictingIndicators;
    
    // Exit quality assessment (1-10)
    private Integer exitQualityScore;

    private String streategy;
    
    /**
     * Add a technical reason for entering the trade
     * 
     * @param code The code for the technical reason
     * @param description Optional description for new custom values
     * @return This instance for method chaining
     */
    public TradeEntryExistReasoning addTechnicalReason(String code, String description) {
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
    public TradeEntryExistReasoning addFundamentalReason(String code, String description) {
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
    public TradeEntryExistReasoning addSupportingIndicator(String indicator) {
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
    public TradeEntryExistReasoning addConflictingIndicator(String indicator) {
        if (conflictingIndicators == null) {
            conflictingIndicators = new ArrayList<>();
        }
        conflictingIndicators.add(indicator);
        return this;
    }
    
    /**
     * Add an exit supporting indicator
     * 
     * @param indicator The supporting indicator or metric for exit
     * @return This instance for method chaining
     */
    public TradeEntryExistReasoning addExitSupportingIndicator(String indicator) {
        if (exitSupportingIndicators == null) {
            exitSupportingIndicators = new ArrayList<>();
        }
        exitSupportingIndicators.add(indicator);
        return this;
    }
    
    /**
     * Add an exit conflicting indicator
     * 
     * @param indicator The conflicting indicator or metric for exit
     * @return This instance for method chaining
     */
    public TradeEntryExistReasoning addExitConflictingIndicator(String indicator) {
        if (exitConflictingIndicators == null) {
            exitConflictingIndicators = new ArrayList<>();
        }
        exitConflictingIndicators.add(indicator);
        return this;
    }
    
    /**
     * Set the exit quality score
     * 
     * @param score The exit quality score (1-10)
     * @return This instance for method chaining
     */
    public TradeEntryExistReasoning setExitQualityScore(Integer score) {
        this.exitQualityScore = score;
        return this;
    }
    
    /**
     * Set the exit primary reason
     * 
     * @param reason The primary reason for exiting the trade
     * @return This instance for method chaining
     */
    public TradeEntryExistReasoning setExitPrimaryReason(String reason) {
        this.exitPrimaryReason = reason;
        return this;
    }
    
    /**
     * Set the exit reasoning summary
     * 
     * @param summary Detailed explanation of exit reasoning
     * @return This instance for method chaining
     */
    public TradeEntryExistReasoning setExitReasoningSummary(String summary) {
        this.exitReasoningSummary = summary;
        return this;
    }
    
    /**
     * Set the exit confidence level
     * 
     * @param level Confidence level in exit analysis (1-10)
     * @return This instance for method chaining
     */
    public TradeEntryExistReasoning setExitConfidenceLevel(Integer level) {
        this.exitConfidenceLevel = level;
        return this;
    }
}
