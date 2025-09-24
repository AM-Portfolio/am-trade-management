package am.trade.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representation of TradePsychologyData for MongoDB storage
 * Stores psychology factors as string codes instead of complex objects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradePsychologyDataEntity {
    
    // Entry psychology factors stored as string codes
    @Builder.Default
    private List<String> entryPsychologyFactors = new ArrayList<>();
    
    // Exit psychology factors stored as string codes
    @Builder.Default
    private List<String> exitPsychologyFactors = new ArrayList<>();
    
    // Behavior patterns stored as string codes
    @Builder.Default
    private List<String> behaviorPatterns = new ArrayList<>();
    
    // Additional psychology notes
    private String psychologyNotes;
    
    // Custom entry psychology descriptions (code -> description mapping)
    @Builder.Default
    private List<PsychologyFactorEntity> customEntryFactors = new ArrayList<>();
    
    // Custom exit psychology descriptions (code -> description mapping)
    @Builder.Default
    private List<PsychologyFactorEntity> customExitFactors = new ArrayList<>();
    
    // Custom behavior pattern descriptions (code -> description mapping)
    @Builder.Default
    private List<PsychologyFactorEntity> customBehaviorPatterns = new ArrayList<>();
}
