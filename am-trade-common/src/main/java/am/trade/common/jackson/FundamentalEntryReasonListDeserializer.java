package am.trade.common.jackson;

import am.trade.common.models.enums.FundamentalEntryReason;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer for lists of FundamentalEntryReason when used in JSON
 */
public class FundamentalEntryReasonListDeserializer extends JsonDeserializer<List<FundamentalEntryReason>> {
    
    @Override
    public List<FundamentalEntryReason> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<FundamentalEntryReason> result = new ArrayList<>();
        
        if (p.currentToken() == JsonToken.START_ARRAY) {
            while (p.nextToken() != JsonToken.END_ARRAY) {
                String code = p.getValueAsString();
                if (code != null && !code.isEmpty()) {
                    result.add(FundamentalEntryReason.fromCode(code));
                }
            }
        }
        
        return result;
    }
}
