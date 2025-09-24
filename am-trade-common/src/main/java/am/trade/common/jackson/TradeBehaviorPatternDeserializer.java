package am.trade.common.jackson;

import am.trade.common.models.enums.TradeBehaviorPattern;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Custom deserializer for TradeBehaviorPattern when used in JSON
 */
public class TradeBehaviorPatternDeserializer extends JsonDeserializer<TradeBehaviorPattern> {
    
    @Override
    public TradeBehaviorPattern deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String code = p.getValueAsString();
        if (code == null || code.isEmpty()) {
            return null;
        }
        
        // Use the fromCode method to get or create a TradeBehaviorPattern instance
        return TradeBehaviorPattern.fromCode(code);
    }
}
