package am.trade.common.models;

import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.common.models.enums.TradeBehaviorPattern;
import am.trade.common.models.enums.TradeTagCategories;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Model class for storing trade psychology data, behavior patterns, and categorized tags
 * Designed to work with the flexible enum system that supports user-provided values
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TradePsychologyData {
    // Entry psychology factors that influenced this trade
    private List<EntryPsychology> entryPsychologyFactors;
    
    // Exit psychology factors that influenced this trade
    private List<ExitPsychology> exitPsychologyFactors;
    
    // Behavior patterns identified in this trade
    private List<TradeBehaviorPattern> behaviorPatterns;
    
    // Tags organized by categories
    private Map<TradeTagCategories, List<String>> categorizedTags;
    
    // Notes about psychological aspects of the trade
    private String psychologyNotes;
    
    /**
     * Add an entry psychology factor to the trade
     * 
     * @param code The code for the entry psychology
     * @param description Optional description for new custom values
     * @return This instance for method chaining
     */
    public TradePsychologyData addEntryPsychology(String code, String description) {
        if (entryPsychologyFactors == null) {
            entryPsychologyFactors = new ArrayList<>();
        }
        entryPsychologyFactors.add(EntryPsychology.fromCode(code, description));
        return this;
    }
    
    /**
     * Add an exit psychology factor to the trade
     * 
     * @param code The code for the exit psychology
     * @param description Optional description for new custom values
     * @return This instance for method chaining
     */
    public TradePsychologyData addExitPsychology(String code, String description) {
        if (exitPsychologyFactors == null) {
            exitPsychologyFactors = new ArrayList<>();
        }
        exitPsychologyFactors.add(ExitPsychology.fromCode(code, description));
        return this;
    }
    
    /**
     * Add a behavior pattern to the trade
     * 
     * @param code The code for the behavior pattern
     * @param description Optional description for new custom values
     * @return This instance for method chaining
     */
    public TradePsychologyData addBehaviorPattern(String code, String description) {
        if (behaviorPatterns == null) {
            behaviorPatterns = new ArrayList<>();
        }
        behaviorPatterns.add(TradeBehaviorPattern.fromCode(code, description));
        return this;
    }
    
    /**
     * Add a tag to a specific category
     * 
     * @param categoryCode The code for the tag category
     * @param categoryDescription Optional description for new custom category
     * @param tag The tag to add
     * @return This instance for method chaining
     */
    public TradePsychologyData addCategorizedTag(String categoryCode, String categoryDescription, String tag) {
        if (categorizedTags == null) {
            categorizedTags = new HashMap<>();
        }
        
        TradeTagCategories category = TradeTagCategories.fromCode(categoryCode, categoryDescription);
        categorizedTags.computeIfAbsent(category, k -> new ArrayList<>()).add(tag);
        return this;
    }
}
