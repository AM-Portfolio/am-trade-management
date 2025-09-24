package am.trade.common.util;

import am.trade.common.jackson.TradeManagementJacksonModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

/**
 * Utility class for JSON conversion operations
 * Provides methods to convert between JSON and model objects
 */
@Component
public class JsonConverter {
    private static final Logger logger = Logger.getLogger(JsonConverter.class.getName());
    private final ObjectMapper objectMapper;

    /**
     * Creates and configures an ObjectMapper instance
     */
    public JsonConverter() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Support for Java 8 date/time types
        objectMapper.registerModule(new TradeManagementJacksonModule()); // Register our custom module for TradeTagCategories
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignore unknown JSON properties
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Use ISO-8601 date format
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print JSON
    }

    /**
     * Convert an object to JSON string
     * @param object Object to convert
     * @return JSON string representation
     */
    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Error converting object to JSON: " + e.getMessage(), e);
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }

    /**
     * Convert a JSON string to an object
     * @param json JSON string to convert
     * @param valueType Class of the target object
     * @param <T> Type of the target object
     * @return Object of type T
     */
    public <T> T fromJson(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Error converting JSON to object: " + e.getMessage(), e);
            throw new RuntimeException("Error converting JSON to object", e);
        }
    }

    /**
     * Read a JSON file and convert to an object
     * @param filePath Path to the JSON file
     * @param valueType Class of the target object
     * @param <T> Type of the target object
     * @return Object of type T
     */
    public <T> T fromJsonFile(String filePath, Class<T> valueType) {
        try {
            Path path = Paths.get(filePath);
            String json = Files.readString(path);
            return fromJson(json, valueType);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading JSON file: " + e.getMessage(), e);
            throw new RuntimeException("Error reading JSON file", e);
        }
    }

    /**
     * Read a JSON file from resources and convert to an object
     * @param resourcePath Path to the resource file
     * @param valueType Class of the target object
     * @param <T> Type of the target object
     * @return Object of type T
     */
    public <T> T fromJsonResource(String resourcePath, Class<T> valueType) {
        try (InputStream is = JsonConverter.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return objectMapper.readValue(is, valueType);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading JSON resource: " + e.getMessage(), e);
            throw new RuntimeException("Error reading JSON resource", e);
        }
    }

    /**
     * Write an object to a JSON file
     * @param object Object to convert
     * @param filePath Path to the output file
     */
    public void toJsonFile(Object object, String filePath) {
        try {
            objectMapper.writeValue(new File(filePath), object);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing object to JSON file: " + e.getMessage(), e);
            throw new RuntimeException("Error writing object to JSON file", e);
        }
    }

    /**
     * Convert a JSON string to a list of objects
     * @param json JSON string to convert
     * @param elementType Class of the list elements
     * @param <T> Type of the list elements
     * @return List of objects of type T
     */
    public <T> List<T> fromJsonToList(String json, Class<T> elementType) {
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Error converting JSON to list: " + e.getMessage(), e);
            throw new RuntimeException("Error converting JSON to list", e);
        }
    }

    /**
     * Read a JSON file and convert to a list of objects
     * @param filePath Path to the JSON file
     * @param elementType Class of the list elements
     * @param <T> Type of the list elements
     * @return List of objects of type T
     */
    public <T> List<T> fromJsonFileToList(String filePath, Class<T> elementType) {
        try {
            Path path = Paths.get(filePath);
            String json = Files.readString(path);
            return fromJsonToList(json, elementType);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading JSON file to list: " + e.getMessage(), e);
            throw new RuntimeException("Error reading JSON file to list", e);
        }
    }

    /**
     * Read a JSON file from resources and convert to a list of objects
     * @param resourcePath Path to the resource file
     * @param elementType Class of the list elements
     * @param <T> Type of the list elements
     * @return List of objects of type T
     */
    public <T> List<T> fromJsonResourceToList(String resourcePath, Class<T> elementType) {
        try (InputStream is = JsonConverter.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return objectMapper.readValue(is,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading JSON resource to list: " + e.getMessage(), e);
            throw new RuntimeException("Error reading JSON resource to list", e);
        }
    }

    /**
     * Get the ObjectMapper instance for custom configuration
     * @return The ObjectMapper instance
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
