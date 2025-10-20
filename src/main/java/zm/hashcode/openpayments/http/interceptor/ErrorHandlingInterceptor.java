package zm.hashcode.openpayments.http.interceptor;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import zm.hashcode.openpayments.http.core.HttpResponse;

/**
 * Response interceptor that handles HTTP errors and extracts error details.
 *
 * <p>
 * This interceptor processes error responses (4xx, 5xx) and extracts structured error information from JSON response
 * bodies. It logs errors and can optionally throw exceptions for error responses.
 *
 * <p>
 * Error information is logged with details including:
 * <ul>
 * <li>Status code</li>
 * <li>Error message from response body</li>
 * <li>Error code (if present)</li>
 * <li>Additional error details</li>
 * </ul>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * ObjectMapper objectMapper = new ObjectMapper();
 * ErrorHandlingInterceptor errorHandler = new ErrorHandlingInterceptor(objectMapper);
 * client.addResponseInterceptor(errorHandler);
 * }</pre>
 */
public record ErrorHandlingInterceptor(ObjectMapper objectMapper) implements ResponseInterceptor {

    private static final Logger LOGGER = Logger.getLogger(ErrorHandlingInterceptor.class.getName());

    /**
     * Creates an error handling interceptor.
     *
     * @param objectMapper
     *            the JSON object mapper for parsing error responses
     */
    public ErrorHandlingInterceptor {
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    public HttpResponse intercept(HttpResponse response) {
        if (response.isSuccessful()) {
            return response;
        }

        // Extract error details from response
        ErrorDetails error = extractErrorDetails(response);

        // Log error
        if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.log(Level.WARNING, "HTTP Error Response: {0} - {1}",
                    new Object[]{response.statusCode(), error.message()});
        }

        if (LOGGER.isLoggable(Level.WARNING) && error.code().isPresent()) {
            LOGGER.log(Level.WARNING, "Error Code: {0}", error.code().get());
        }

        if (LOGGER.isLoggable(Level.FINE) && error.details().isPresent()) {
            LOGGER.log(Level.FINE, "Error Details: {0}", error.details().get());
        }

        return response;
    }

    private ErrorDetails extractErrorDetails(HttpResponse response) {
        String body = response.body();

        if (body == null || body.isEmpty()) {
            return new ErrorDetails("HTTP " + response.statusCode(), Optional.empty(), Optional.empty());
        }

        // Try to parse as JSON
        try {
            JsonNode root = objectMapper.readTree(body);

            String message = extractField(root, "message", "error", "error_description", "title")
                    .orElse("HTTP " + response.statusCode());

            Optional<String> code = extractField(root, "code", "error_code", "type");
            Optional<String> details = extractField(root, "details", "description", "detail");

            return new ErrorDetails(message, code, details);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // Not valid JSON, use body as message
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Could not parse error response as JSON", e);
            }
            return new ErrorDetails(body, Optional.empty(), Optional.empty());
        }
    }

    private Optional<String> extractField(JsonNode root, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = root.get(fieldName);
            if (field != null && !field.isNull()) {
                return Optional.of(field.asText());
            }
        }
        return Optional.empty();
    }

    /**
     * Structured error details extracted from HTTP error responses.
     *
     * @param message
     *            the error message
     * @param code
     *            the error code (optional)
     * @param details
     *            additional error details (optional)
     */
    public record ErrorDetails(String message, Optional<String> code, Optional<String> details) {

        public ErrorDetails {
            Objects.requireNonNull(message, "message must not be null");
            code = Optional.ofNullable(code).orElse(Optional.empty());
            details = Optional.ofNullable(details).orElse(Optional.empty());
        }
    }
}
