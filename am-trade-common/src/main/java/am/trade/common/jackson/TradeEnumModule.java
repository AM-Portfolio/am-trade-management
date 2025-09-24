package am.trade.common.jackson;

import am.trade.common.models.enums.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Custom Jackson module for trade-specific enum-like classes serialization/deserialization
 */
public class TradeEnumModule extends SimpleModule {
    
    private static final long serialVersionUID = 1L;
    
    public TradeEnumModule() {
        super("TradeEnumModule");
        
        // Register key deserializers for all enum-like classes
        addKeyDeserializer(TradeTagCategories.class, new EnumLikeKeyDeserializer(TradeTagCategories.class));
        addKeyDeserializer(TechnicalEntryReason.class, new EnumLikeKeyDeserializer(TechnicalEntryReason.class));
        addKeyDeserializer(FundamentalEntryReason.class, new EnumLikeKeyDeserializer(FundamentalEntryReason.class));
        addKeyDeserializer(EntryPsychology.class, new EnumLikeKeyDeserializer(EntryPsychology.class));
        addKeyDeserializer(ExitPsychology.class, new EnumLikeKeyDeserializer(ExitPsychology.class));
        addKeyDeserializer(TradeBehaviorPattern.class, new EnumLikeKeyDeserializer(TradeBehaviorPattern.class));
    }
}
