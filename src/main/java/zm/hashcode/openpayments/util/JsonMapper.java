package zm.hashcode.openpayments.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import zm.hashcode.openpayments.model.OpenPaymentsException;

/**
 * Utility class for JSON serialization and deserialization using Jackson.
 *
 * <p>
 * This class provides a configured ObjectMapper instance for Open Payments API interactions, with support for Java 8
 * date/time types and other common configurations.
 */
public final class JsonMapper {

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private JsonMapper() {
        // Utility class
    }

    /**
     * Returns the configured ObjectMapper instance.
     *
     * @return the ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Serializes an object to JSON string.
     *
     * @param object
     *            the object to serialize
     * @return the JSON string
     * @throws OpenPaymentsException
     *             if serialization fails
     */
    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new OpenPaymentsException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Deserializes a JSON string to an object of the specified type.
     *
     * @param json
     *            the JSON string
     * @param type
     *            the target type
     * @param <T>
     *            the type parameter
     * @return the deserialized object
     * @throws OpenPaymentsException
     *             if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new OpenPaymentsException("Failed to deserialize JSON to " + type.getName(), e);
        }
    }

    /**
     * Deserializes a JSON string to an object using a TypeReference.
     *
     * @param json
     *            the JSON string
     * @param typeRef
     *            the type reference
     * @param <T>
     *            the type parameter
     * @return the deserialized object
     * @throws OpenPaymentsException
     *             if deserialization fails
     */
    public static <T> T fromJson(String json, com.fasterxml.jackson.core.type.TypeReference<T> typeRef) {
        try {
            return OBJECT_MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new OpenPaymentsException("Failed to deserialize JSON", e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        var mapper = new ObjectMapper();

        // Register module for Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());

        // Configure serialization
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        // Configure deserialization
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);

        return mapper;
    }
}
