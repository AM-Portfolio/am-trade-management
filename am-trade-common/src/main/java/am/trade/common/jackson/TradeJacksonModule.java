package am.trade.common.jackson;

import am.trade.common.models.enums.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

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
        
        // Add other enum-like classes as needed
        registerEnumLikeClass(TechnicalEntryReason.class);
        registerEnumLikeClass(FundamentalEntryReason.class);
        registerEnumLikeClass(EntryPsychology.class);
        registerEnumLikeClass(ExitPsychology.class);
        registerEnumLikeClass(TradeBehaviorPattern.class);
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
     * Key deserializer for enum-like classes when used as Map keys
     */
    private static class EnumLikeKeyDeserializer extends KeyDeserializer {
        
        private final Class<?> targetClass;
        
        public EnumLikeKeyDeserializer(Class<?> targetClass) {
            this.targetClass = targetClass;
        }
        
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            if (key == null || key.isEmpty()) {
                return null;
            }
            
            try {
                // Assume all enum-like classes have a fromCode(String) method
                return targetClass.getMethod("fromCode", String.class).invoke(null, key);
            } catch (Exception e) {
                return null; // Return null on error to avoid breaking deserialization
            }
        }
    }
}
