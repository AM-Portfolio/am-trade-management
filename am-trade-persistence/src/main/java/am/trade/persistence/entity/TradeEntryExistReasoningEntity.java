package am.trade.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representation of TradeEntryReasoning for MongoDB storage
 * Stores entry reasoning as simple strings instead of complex objects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeEntryExistReasoningEntity {
    
    // Technical analysis factors stored as strings
    private List<String> technicalReasons;
    
    // Fundamental analysis factors stored as strings
    private List<String> fundamentalReasons;
    
    // Additional reasoning notes
    private String reasoningNotes;

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
}
