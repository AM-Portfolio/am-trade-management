package am.trade.common.jackson;

import am.trade.common.models.enums.TechnicalEntryReason;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer for lists of TechnicalEntryReason when used in JSON
 */
public class TechnicalEntryReasonListDeserializer extends JsonDeserializer<List<TechnicalEntryReason>> {
    
    @Override
    public List<TechnicalEntryReason> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<TechnicalEntryReason> result = new ArrayList<>();
        
        if (p.currentToken() == JsonToken.START_ARRAY) {
            while (p.nextToken() != JsonToken.END_ARRAY) {
                String code = p.getValueAsString();
                if (code != null && !code.isEmpty()) {
                    result.add(TechnicalEntryReason.fromCode(code));
                }
            }
        }
        
        return result;
    }
}
