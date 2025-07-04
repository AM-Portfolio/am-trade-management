package am.trade.common.jackson;

import java.lang.reflect.Method;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Key deserializer for enum-like classes that use the static factory pattern
 * with a fromCode method. Used for deserializing map keys.
 */
public class EnumLikeKeyDeserializer extends KeyDeserializer {
    
    private static final Logger log = LoggerFactory.getLogger(EnumLikeKeyDeserializer.class);
    private final Class<?> targetClass;
    
    public EnumLikeKeyDeserializer(Class<?> targetClass) {
        this.targetClass = targetClass;
    }
    
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) {
        if (key == null) {
            return null;
        }
        
        try {
            // Try to find the fromCode(String) method
            Method fromCodeMethod = targetClass.getMethod("fromCode", String.class);
            return fromCodeMethod.invoke(null, key);
        } catch (Exception e) {
            log.error("Error deserializing key '{}' for class {}: {}", key, targetClass.getName(), e.getMessage());
            return null;
        }
    }
}
