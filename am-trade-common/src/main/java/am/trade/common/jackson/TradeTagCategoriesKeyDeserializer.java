package am.trade.common.jackson;

import am.trade.common.models.enums.TradeTagCategories;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.io.IOException;

/**
 * Custom KeyDeserializer for TradeTagCategories when used as a Map key in JSON
 */
public class TradeTagCategoriesKeyDeserializer extends KeyDeserializer {
    
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        if (key == null || key.isEmpty()) {
            return null;
        }
        
        // Use the fromCode method to get or create a TradeTagCategories instance
        return TradeTagCategories.fromCode(key);
    }
}
