package am.trade.common.jackson;

import am.trade.common.models.Attachment;
import am.trade.common.models.enums.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Jackson module for trade-specific serialization/deserialization
 * Handles enum-like classes that use static factory pattern
 */
public class TradeJacksonModule extends SimpleModule {
    
    private static final long serialVersionUID = 1L;
    
    public TradeJacksonModule() {
        super("TradeJacksonModule");
        
        // Register serializers and deserializers for TradeTagCategories
        addSerializer(TradeTagCategories.class, new EnumLikeSerializer<>(TradeTagCategories.class));
        addDeserializer(TradeTagCategories.class, new EnumLikeDeserializer<>(TradeTagCategories.class));
        addKeySerializer(TradeTagCategories.class, new EnumLikeKeySerializer<>(TradeTagCategories.class));
        addKeyDeserializer(TradeTagCategories.class, new EnumLikeKeyDeserializer(TradeTagCategories.class));
        
        // Add other enum-like classes
        registerEnumLikeClass(TechnicalEntryReason.class);
        registerEnumLikeClass(FundamentalEntryReason.class);
        registerEnumLikeClass(EntryPsychology.class);
        registerEnumLikeClass(ExitPsychology.class);
        registerEnumLikeClass(TradeBehaviorPattern.class);
        
        // Register list deserializers for collections of enum-like objects
        addDeserializer(List.class, new EnumLikeListDeserializer());
    }
    
    private <T> void registerEnumLikeClass(Class<T> clazz) {
        addSerializer(clazz, new EnumLikeSerializer<>(clazz));
        addDeserializer(clazz, new EnumLikeDeserializer<>(clazz));
        addKeySerializer(clazz, new EnumLikeKeySerializer<>(clazz));
        addKeyDeserializer(clazz, new EnumLikeKeyDeserializer(clazz));
    }
    
    /**
     * Serializer for enum-like classes
     */
    private static class EnumLikeSerializer<T> extends StdSerializer<T> {
        
        protected EnumLikeSerializer(Class<T> t) {
            super(t);
        }
        
        @Override
        public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            
            try {
                // Assume all enum-like classes have a getCode() method
                Object code = value.getClass().getMethod("getCode").invoke(value);
                gen.writeString(code.toString());
            } catch (Exception e) {
                throw new IOException("Failed to serialize enum-like class: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Deserializer for enum-like classes
     */
    private static class EnumLikeDeserializer<T> extends StdDeserializer<T> {
        
        protected EnumLikeDeserializer(Class<T> t) {
            super(t);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null || value.isEmpty()) {
                return null;
            }
            
            try {
                // Assume all enum-like classes have a fromCode(String) method
                return (T) _valueClass.getMethod("fromCode", String.class).invoke(null, value);
            } catch (Exception e) {
                throw new IOException("Failed to deserialize enum-like class: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Key serializer for enum-like classes when used as Map keys
     */
    private static class EnumLikeKeySerializer<T> extends StdSerializer<T> {
        
        protected EnumLikeKeySerializer(Class<T> t) {
            super(t);
        }
        
        @Override
        public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeFieldName("null");
                return;
            }
            
            try {
                // Assume all enum-like classes have a getCode() method
                Object code = value.getClass().getMethod("getCode").invoke(value);
                gen.writeFieldName(code.toString());
            } catch (Exception e) {
                throw new IOException("Failed to serialize enum-like class key: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Deserializer for lists of enum-like objects
     * This handles cases where a JSON array of strings needs to be converted to a list of enum-like objects
     */
    private static class EnumLikeListDeserializer extends StdDeserializer<List<?>> {
        
        public EnumLikeListDeserializer() {
            super(List.class);
        }
        
        @Override
        public List<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            // Get the containing property name to determine what type of list this is
            String fieldName = p.getParsingContext().getCurrentName();
            
            // Handle specific list types based on the field name
            if (fieldName != null) {
                if (fieldName.equals("technicalReasons")) {
                    return deserializeEnumLikeList(p, TechnicalEntryReason.class);
                } else if (fieldName.equals("fundamentalReasons")) {
                    return deserializeEnumLikeList(p, FundamentalEntryReason.class);
                } else if (fieldName.equals("entryPsychologyFactors")) {
                    return deserializeEnumLikeList(p, EntryPsychology.class);
                } else if (fieldName.equals("exitPsychologyFactors")) {
                    return deserializeEnumLikeList(p, ExitPsychology.class);
                } else if (fieldName.equals("behaviorPatterns")) {
                    return deserializeEnumLikeList(p, TradeBehaviorPattern.class);
                } else if (fieldName.equals("attachments")) {
                    // Handle attachments separately as they're not enum-like objects
                    return deserializeAttachmentList(p, ctxt);
                }
            }
            
            // Fall back to default list deserialization
            CollectionType listType = ctxt.getTypeFactory().constructCollectionType(ArrayList.class, Object.class);
            return ctxt.readValue(p, listType);
        }
        
        private <T> List<T> deserializeEnumLikeList(JsonParser p, Class<T> elementType) throws IOException {
            List<T> result = new ArrayList<>();
            
            if (p.currentToken() == JsonToken.START_ARRAY) {
                while (p.nextToken() != JsonToken.END_ARRAY) {
                    // Skip null values
                    if (p.currentToken() == JsonToken.VALUE_NULL) {
                        continue;
                    }
                    
                    String code = p.getValueAsString();
                    if (code != null && !code.isEmpty()) {
                        try {
                            @SuppressWarnings("unchecked")
                            T value = (T) elementType.getMethod("fromCode", String.class).invoke(null, code);
                            if (value != null) {
                                result.add(value);
                            }
                        } catch (Exception e) {
                            // Log but continue processing
                            System.err.println("Failed to deserialize enum-like value: " + code + " - " + e.getMessage());
                        }
                    }
                }
            }
            
            return result;
        }
        
        /**
         * Deserialize a list of Attachment objects
         * This handles the special case for attachments which are not enum-like objects
         */
        private List<Attachment> deserializeAttachmentList(JsonParser p, DeserializationContext ctxt) throws IOException {
            CollectionType listType = ctxt.getTypeFactory().constructCollectionType(ArrayList.class, Attachment.class);
            return ctxt.readValue(p, listType);
        }
    }
}
