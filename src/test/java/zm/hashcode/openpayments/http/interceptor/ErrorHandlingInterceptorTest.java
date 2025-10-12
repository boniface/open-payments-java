package zm.hashcode.openpayments.http.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import zm.hashcode.openpayments.http.core.HttpResponse;
import zm.hashcode.openpayments.http.interceptor.ErrorHandlingInterceptor.ErrorDetails;

/**
 * Unit tests for {@link ErrorHandlingInterceptor}.
 */
@DisplayName("ErrorHandlingInterceptor")
class ErrorHandlingInterceptorTest {

    private ObjectMapper objectMapper;
    private ErrorHandlingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jdk8.Jdk8Module());
        interceptor = new ErrorHandlingInterceptor(objectMapper);
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should construct with valid object mapper")
        void shouldConstructWithValidObjectMapper() {
            assertThat(interceptor).isNotNull();
        }

        @Test
        @DisplayName("should throw when object mapper is null")
        void shouldThrowWhenObjectMapperIsNull() {
            assertThatThrownBy(() -> new ErrorHandlingInterceptor(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("objectMapper must not be null");
        }
    }

    @Nested
    @DisplayName("Successful Responses")
    class SuccessfulResponsesTests {

        @Test
        @DisplayName("should not modify 200 OK responses")
        void shouldNotModify200Response() {
            HttpResponse response = new HttpResponse(200, Map.of(), "{\"status\":\"success\"}");

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should not modify 201 Created responses")
        void shouldNotModify201Response() {
            HttpResponse response = new HttpResponse(201, Map.of(), "{\"id\":\"123\"}");

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should not modify 204 No Content responses")
        void shouldNotModify204Response() {
            HttpResponse response = new HttpResponse(204, Map.of(), "");

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("Error Response Handling")
    class ErrorResponseHandlingTests {

        @Test
        @DisplayName("should handle 400 Bad Request with JSON error")
        void shouldHandle400WithJsonError() {
            String errorBody = """
                    {
                        "error": "invalid_request",
                        "error_description": "Missing required parameter: amount"
                    }
                    """;

            HttpResponse response = new HttpResponse(400, Map.of("Content-Type", "application/json"), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
            assertThat(result.statusCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("should handle 401 Unauthorized with message field")
        void shouldHandle401WithMessage() {
            String errorBody = """
                    {
                        "message": "Invalid access token",
                        "code": "UNAUTHORIZED"
                    }
                    """;

            HttpResponse response = new HttpResponse(401, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle 403 Forbidden with details")
        void shouldHandle403WithDetails() {
            String errorBody = """
                    {
                        "error": "insufficient_permissions",
                        "details": "User does not have permission to access this resource"
                    }
                    """;

            HttpResponse response = new HttpResponse(403, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle 404 Not Found")
        void shouldHandle404() {
            String errorBody = """
                    {
                        "error": "not_found",
                        "message": "Resource not found"
                    }
                    """;

            HttpResponse response = new HttpResponse(404, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle 409 Conflict")
        void shouldHandle409() {
            String errorBody = """
                    {
                        "error": "resource_conflict",
                        "message": "Resource already exists",
                        "code": "CONFLICT"
                    }
                    """;

            HttpResponse response = new HttpResponse(409, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle 422 Unprocessable Entity")
        void shouldHandle422() {
            String errorBody = """
                    {
                        "error": "validation_error",
                        "message": "Invalid input data",
                        "details": "Amount must be positive"
                    }
                    """;

            HttpResponse response = new HttpResponse(422, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle 429 Too Many Requests")
        void shouldHandle429() {
            String errorBody = """
                    {
                        "error": "rate_limit_exceeded",
                        "message": "Too many requests"
                    }
                    """;

            HttpResponse response = new HttpResponse(429, Map.of("X-RateLimit-Reset", "1234567890"), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle 500 Internal Server Error")
        void shouldHandle500() {
            String errorBody = """
                    {
                        "error": "internal_server_error",
                        "message": "An unexpected error occurred"
                    }
                    """;

            HttpResponse response = new HttpResponse(500, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle 502 Bad Gateway")
        void shouldHandle502() {
            String errorBody = """
                    {
                        "error": "bad_gateway",
                        "message": "Upstream service unavailable"
                    }
                    """;

            HttpResponse response = new HttpResponse(502, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle 503 Service Unavailable")
        void shouldHandle503() {
            String errorBody = """
                    {
                        "error": "service_unavailable",
                        "message": "Service temporarily unavailable"
                    }
                    """;

            HttpResponse response = new HttpResponse(503, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("Error Parsing")
    class ErrorParsingTests {

        @Test
        @DisplayName("should handle error with 'error' field")
        void shouldHandleErrorField() {
            String errorBody = "{\"error\":\"invalid_request\"}";

            HttpResponse response = new HttpResponse(400, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle error with 'message' field")
        void shouldHandleMessageField() {
            String errorBody = "{\"message\":\"Something went wrong\"}";

            HttpResponse response = new HttpResponse(500, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle error with 'title' field")
        void shouldHandleTitleField() {
            String errorBody = "{\"title\":\"Bad Request\",\"detail\":\"Invalid parameters\"}";

            HttpResponse response = new HttpResponse(400, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle error with 'error_code' field")
        void shouldHandleErrorCodeField() {
            String errorBody = "{\"error_code\":\"ERR_001\",\"message\":\"Error occurred\"}";

            HttpResponse response = new HttpResponse(500, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle error with 'type' field")
        void shouldHandleTypeField() {
            String errorBody = "{\"type\":\"validation_error\",\"message\":\"Invalid input\"}";

            HttpResponse response = new HttpResponse(422, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle error with 'description' field")
        void shouldHandleDescriptionField() {
            String errorBody = "{\"error\":\"failed\",\"description\":\"Operation failed\"}";

            HttpResponse response = new HttpResponse(500, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle non-JSON error responses")
        void shouldHandleNonJsonErrors() {
            String errorBody = "Internal Server Error";

            HttpResponse response = new HttpResponse(500, Map.of(), errorBody);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle empty error body")
        void shouldHandleEmptyErrorBody() {
            HttpResponse response = new HttpResponse(500, Map.of(), "");

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle null error body")
        void shouldHandleNullErrorBody() {
            HttpResponse response = new HttpResponse(500, Map.of(), null);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle malformed JSON")
        void shouldHandleMalformedJson() {
            String malformedJson = "{\"error\": invalid json}";

            HttpResponse response = new HttpResponse(400, Map.of(), malformedJson);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("ErrorDetails")
    class ErrorDetailsTests {

        @Test
        @DisplayName("should create error details with all fields")
        void shouldCreateErrorDetailsWithAllFields() {
            ErrorDetails details = new ErrorDetails("Error message", java.util.Optional.of("ERR_001"),
                    java.util.Optional.of("Additional details"));

            assertThat(details.message()).isEqualTo("Error message");
            assertThat(details.code()).isPresent().contains("ERR_001");
            assertThat(details.details()).isPresent().contains("Additional details");
        }

        @Test
        @DisplayName("should create error details with message only")
        void shouldCreateErrorDetailsWithMessageOnly() {
            ErrorDetails details = new ErrorDetails("Error message", null, null);

            assertThat(details.message()).isEqualTo("Error message");
            assertThat(details.code()).isEmpty();
            assertThat(details.details()).isEmpty();
        }

        @Test
        @DisplayName("should throw when message is null")
        void shouldThrowWhenMessageIsNull() {
            assertThatThrownBy(() -> new ErrorDetails(null, null, null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("message must not be null");
        }
    }
}
