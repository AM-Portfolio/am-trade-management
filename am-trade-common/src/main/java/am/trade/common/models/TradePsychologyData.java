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
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    
    // Tags organized by categories - using String keys for MongoDB compatibility
    private Map<String, List<String>> categorizedTags;
    
    // Notes about psychological aspects of the trade
    private String psychologyNotes;
    
    /**
     * Get entry psychology factors, filtering out null values
     * This method is used during JSON serialization to exclude nulls
     */
    @JsonProperty("entryPsychologyFactors")
    public List<EntryPsychology> getEntryPsychologyFactors() {
        if (entryPsychologyFactors == null || entryPsychologyFactors.isEmpty()) {
            return null;
        }
        List<EntryPsychology> filtered = entryPsychologyFactors.stream()
            .filter(p -> p != null)
            .collect(Collectors.toList());
        return filtered.isEmpty() ? null : filtered;
    }
    
    /**
     * Get exit psychology factors, filtering out null values
     * This method is used during JSON serialization to exclude nulls
     */
    @JsonProperty("exitPsychologyFactors")
    public List<ExitPsychology> getExitPsychologyFactors() {
        if (exitPsychologyFactors == null || exitPsychologyFactors.isEmpty()) {
            return null;
        }
        List<ExitPsychology> filtered = exitPsychologyFactors.stream()
            .filter(p -> p != null)
            .collect(Collectors.toList());
        return filtered.isEmpty() ? null : filtered;
    }
    
    /**
     * Get behavior patterns, filtering out null values
     * This method is used during JSON serialization to exclude nulls
     */
    @JsonProperty("behaviorPatterns")
    public List<TradeBehaviorPattern> getBehaviorPatterns() {
        if (behaviorPatterns == null || behaviorPatterns.isEmpty()) {
            return null;
        }
        List<TradeBehaviorPattern> filtered = behaviorPatterns.stream()
            .filter(p -> p != null)
            .collect(Collectors.toList());
        return filtered.isEmpty() ? null : filtered;
    }
    
    /**
     * Set entry psychology factors (for deserialization)
     */
    public void setEntryPsychologyFactors(List<EntryPsychology> entryPsychologyFactors) {
        this.entryPsychologyFactors = entryPsychologyFactors;
    }
    
    /**
     * Set exit psychology factors (for deserialization)
     */
    public void setExitPsychologyFactors(List<ExitPsychology> exitPsychologyFactors) {
        this.exitPsychologyFactors = exitPsychologyFactors;
    }
    
    /**
     * Set behavior patterns (for deserialization)
     */
    public void setBehaviorPatterns(List<TradeBehaviorPattern> behaviorPatterns) {
        this.behaviorPatterns = behaviorPatterns;
    }
    
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
     * @param categoryDescription Optional description for new custom category (not used, kept for backward compatibility)
     * @param tag The tag to add
     * @return This instance for method chaining
     */
    public TradePsychologyData addCategorizedTag(String categoryCode, String categoryDescription, String tag) {
        if (categorizedTags == null) {
            categorizedTags = new HashMap<>();
        }
        
        categorizedTags.computeIfAbsent(categoryCode, k -> new ArrayList<>()).add(tag);
        return this;
    }
}
