package am.trade.common.jackson;

import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.common.models.enums.FundamentalEntryReason;
import am.trade.common.models.enums.TechnicalEntryReason;
import am.trade.common.models.enums.TradeBehaviorPattern;
import am.trade.common.models.enums.TradeTagCategories;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson module for Trade Management specific serializers and deserializers
 */
public class TradeManagementJacksonModule extends SimpleModule {
    
    private static final long serialVersionUID = 1L;
    
    public TradeManagementJacksonModule() {
        super("TradeManagementModule");
        
        // Register the key deserializer for TradeTagCategories
        addKeyDeserializer(TradeTagCategories.class, new TradeTagCategoriesKeyDeserializer());
        
        // Register deserializers for custom enum-like classes
        addDeserializer(EntryPsychology.class, new EntryPsychologyDeserializer());
        addDeserializer(ExitPsychology.class, new ExitPsychologyDeserializer());
        addDeserializer(TradeBehaviorPattern.class, new TradeBehaviorPatternDeserializer());
        
        // Register mixins for custom enum-like classes used in collections
        setMixInAnnotation(TechnicalEntryReason.class, TechnicalEntryReasonMixin.class);
        setMixInAnnotation(FundamentalEntryReason.class, FundamentalEntryReasonMixin.class);
    }
}
