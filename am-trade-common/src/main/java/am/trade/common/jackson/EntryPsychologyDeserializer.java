package am.trade.common.jackson;

import am.trade.common.models.enums.EntryPsychology;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Custom deserializer for EntryPsychology when used in JSON
 */
public class EntryPsychologyDeserializer extends JsonDeserializer<EntryPsychology> {
    
    @Override
    public EntryPsychology deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // Handle both string and object formats
        if (p.currentToken() == com.fasterxml.jackson.core.JsonToken.START_OBJECT) {
            // Object format with code and description
            String code = null;
            String description = null;
            
            while (p.nextToken() != com.fasterxml.jackson.core.JsonToken.END_OBJECT) {
                String fieldName = p.currentName();
                p.nextToken(); // move to value
                
                if ("code".equals(fieldName)) {
                    code = p.getValueAsString();
                } else if ("description".equals(fieldName)) {
                    description = p.getValueAsString();
                } else {
                    // Skip unknown fields
                    p.skipChildren();
                }
            }
            
            if (code != null) {
                if (description != null) {
                    // Create with custom description
                    return EntryPsychology.fromCode(code, description);
                } else {
                    // Just use the code
                    return EntryPsychology.fromCode(code);
                }
            }
            return null;
        } else {
            // Simple string format
            String code = p.getValueAsString();
            if (code == null || code.isEmpty()) {
                return null;
            }
            
            // Use the fromCode method to get or create an EntryPsychology instance
            return EntryPsychology.fromCode(code);
        }
    }
}
