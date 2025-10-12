package zm.hashcode.openpayments.http.interceptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import zm.hashcode.openpayments.http.core.HttpResponse;

/**
 * Unit tests for {@link LoggingResponseInterceptor}.
 */
@DisplayName("LoggingResponseInterceptor")
class LoggingResponseInterceptorTest {

    private LoggingResponseInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new LoggingResponseInterceptor();
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should construct with default settings")
        void shouldConstructWithDefaults() {
            assertThat(interceptor).isNotNull();
        }

        @Test
        @DisplayName("should construct with custom log levels")
        void shouldConstructWithCustomLogLevels() {
            LoggingResponseInterceptor custom = new LoggingResponseInterceptor(Level.FINE, Level.SEVERE, true, true);
            assertThat(custom).isNotNull();
        }
    }

    @Nested
    @DisplayName("Response Logging")
    class ResponseLoggingTests {

        @Test
        @DisplayName("should not modify successful response")
        void shouldNotModifySuccessfulResponse() {
            HttpResponse response = new HttpResponse(200, Map.of("Content-Type", "application/json"),
                    "{\"status\":\"success\"}");

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should not modify error response")
        void shouldNotModifyErrorResponse() {
            HttpResponse response = new HttpResponse(404, Map.of("Content-Type", "application/json"),
                    "{\"error\":\"Not Found\"}");

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should handle 2xx successful responses")
        void shouldHandle2xxResponses() {
            for (int status = 200; status < 300; status++) {
                HttpResponse response = new HttpResponse(status, Map.of(), "OK");

                HttpResponse result = interceptor.intercept(response);

                assertThat(result).isSameAs(response);
                assertThat(result.isSuccessful()).isTrue();
            }
        }

        @Test
        @DisplayName("should handle 4xx client error responses")
        void shouldHandle4xxResponses() {
            int[] clientErrors = {400, 401, 403, 404, 409, 422, 429};

            for (int status : clientErrors) {
                HttpResponse response = new HttpResponse(status, Map.of(), "Client Error");

                HttpResponse result = interceptor.intercept(response);

                assertThat(result).isSameAs(response);
                assertThat(result.isSuccessful()).isFalse();
            }
        }

        @Test
        @DisplayName("should handle 5xx server error responses")
        void shouldHandle5xxResponses() {
            int[] serverErrors = {500, 502, 503, 504};

            for (int status : serverErrors) {
                HttpResponse response = new HttpResponse(status, Map.of(), "Server Error");

                HttpResponse result = interceptor.intercept(response);

                assertThat(result).isSameAs(response);
                assertThat(result.isSuccessful()).isFalse();
            }
        }

        @Test
        @DisplayName("should handle response with headers")
        void shouldHandleResponseWithHeaders() {
            Map<String, String> headers = Map.of("Content-Type", "application/json", "X-Request-Id", "abc-123",
                    "X-RateLimit-Remaining", "99");

            HttpResponse response = new HttpResponse(200, headers, "{\"data\":\"test\"}");

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
            assertThat(result.headers()).hasSize(3);
        }

        @Test
        @DisplayName("should handle response with body")
        void shouldHandleResponseWithBody() {
            LoggingResponseInterceptor loggingWithBody = new LoggingResponseInterceptor(Level.INFO, Level.WARNING, true,
                    true);

            HttpResponse response = new HttpResponse(200, Map.of(), "{\"message\":\"Success\",\"data\":{}}");

            HttpResponse result = loggingWithBody.intercept(response);

            assertThat(result).isSameAs(response);
            assertThat(result.getBody()).isNotEmpty();
        }

        @Test
        @DisplayName("should handle response with large body")
        void shouldHandleResponseWithLargeBody() {
            LoggingResponseInterceptor loggingWithBody = new LoggingResponseInterceptor(Level.INFO, Level.WARNING,
                    false, true);

            String largeBody = "x".repeat(2000);
            HttpResponse response = new HttpResponse(200, Map.of(), largeBody);

            HttpResponse result = loggingWithBody.intercept(response);

            assertThat(result).isSameAs(response);
            assertThat(result.body()).hasSize(2000);
        }

        @Test
        @DisplayName("should handle response with empty body")
        void shouldHandleResponseWithEmptyBody() {
            HttpResponse response = new HttpResponse(204, Map.of(), "");

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
            assertThat(result.body()).isEmpty();
        }

        @Test
        @DisplayName("should handle response without headers")
        void shouldHandleResponseWithoutHeaders() {
            HttpResponse response = new HttpResponse(200, Map.of(), "OK");

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
            assertThat(result.headers()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Configuration")
    class ConfigurationTests {

        @Test
        @DisplayName("should support headers-only logging")
        void shouldSupportHeadersOnlyLogging() {
            LoggingResponseInterceptor headersOnly = new LoggingResponseInterceptor(Level.INFO, Level.WARNING, true,
                    false);

            HttpResponse response = new HttpResponse(200, Map.of("Content-Type", "application/json"),
                    "{\"large\":\"body\"}");

            HttpResponse result = headersOnly.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should support no headers or body logging")
        void shouldSupportNoHeadersOrBodyLogging() {
            LoggingResponseInterceptor minimal = new LoggingResponseInterceptor(Level.INFO, Level.WARNING, false,
                    false);

            HttpResponse response = new HttpResponse(200, Map.of("Content-Type", "application/json"),
                    "{\"data\":\"test\"}");

            HttpResponse result = minimal.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should support full logging")
        void shouldSupportFullLogging() {
            LoggingResponseInterceptor full = new LoggingResponseInterceptor(Level.FINE, Level.SEVERE, true, true);

            HttpResponse response = new HttpResponse(200, Map.of("Content-Type", "application/json"),
                    "{\"full\":\"logging\"}");

            HttpResponse result = full.intercept(response);

            assertThat(result).isSameAs(response);
        }

        @Test
        @DisplayName("should use different log levels for success and error")
        void shouldUseDifferentLogLevelsForSuccessAndError() {
            LoggingResponseInterceptor custom = new LoggingResponseInterceptor(Level.FINE, Level.SEVERE, false, false);

            HttpResponse successResponse = new HttpResponse(200, Map.of(), "OK");
            HttpResponse errorResponse = new HttpResponse(500, Map.of(), "Error");

            HttpResponse successResult = custom.intercept(successResponse);
            HttpResponse errorResult = custom.intercept(errorResponse);

            assertThat(successResult).isSameAs(successResponse);
            assertThat(errorResult).isSameAs(errorResponse);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle 3xx redirect responses")
        void shouldHandle3xxResponses() {
            int[] redirects = {301, 302, 303, 307, 308};

            for (int status : redirects) {
                HttpResponse response = new HttpResponse(status, Map.of("Location", "https://example.com/new"), "");

                HttpResponse result = interceptor.intercept(response);

                assertThat(result).isSameAs(response);
            }
        }

        @Test
        @DisplayName("should handle unusual status codes")
        void shouldHandleUnusualStatusCodes() {
            int[] unusualCodes = {100, 101, 102, 418, 451};

            for (int status : unusualCodes) {
                HttpResponse response = new HttpResponse(status, Map.of(), "");

                HttpResponse result = interceptor.intercept(response);

                assertThat(result).isSameAs(response);
            }
        }

        @Test
        @DisplayName("should handle null body")
        void shouldHandleNullBody() {
            HttpResponse response = new HttpResponse(204, Map.of(), null);

            HttpResponse result = interceptor.intercept(response);

            assertThat(result).isSameAs(response);
        }
    }
}
