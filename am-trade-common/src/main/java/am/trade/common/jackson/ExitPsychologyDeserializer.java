package am.trade.common.jackson;

import am.trade.common.models.enums.ExitPsychology;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Custom deserializer for ExitPsychology when used in JSON
 */
public class ExitPsychologyDeserializer extends JsonDeserializer<ExitPsychology> {
    
    @Override
    public ExitPsychology deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String code = p.getValueAsString();
        if (code == null || code.isEmpty()) {
            return null;
        }
        
        // Use the fromCode method to get or create an ExitPsychology instance
        return ExitPsychology.fromCode(code);
    }
}
