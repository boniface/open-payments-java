package zm.hashcode.openpayments.http.interceptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Map;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import zm.hashcode.openpayments.http.core.HttpMethod;
import zm.hashcode.openpayments.http.core.HttpRequest;

/**
 * Unit tests for {@link LoggingRequestInterceptor}.
 */
@DisplayName("LoggingRequestInterceptor")
class LoggingRequestInterceptorTest {

    private LoggingRequestInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new LoggingRequestInterceptor();
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
        @DisplayName("should construct with custom log level")
        void shouldConstructWithCustomLogLevel() {
            LoggingRequestInterceptor custom = new LoggingRequestInterceptor(Level.FINE, true, false);
            assertThat(custom).isNotNull();
        }
    }

    @Nested
    @DisplayName("Request Logging")
    class RequestLoggingTests {

        @Test
        @DisplayName("should not modify request")
        void shouldNotModifyRequest() {
            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri(URI.create("https://example.com/api")).headers(Map.of("Content-Type", "application/json"))
                    .build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result).isSameAs(request);
        }

        @Test
        @DisplayName("should handle request with headers")
        void shouldHandleRequestWithHeaders() {
            HttpRequest request = HttpRequest.builder().method(HttpMethod.POST)
                    .uri(URI.create("https://example.com/api/payments"))
                    .headers(Map.of("Content-Type", "application/json", "Accept", "application/json")).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result).isNotNull();
            assertThat(result.method()).isEqualTo(HttpMethod.POST);
            assertThat(result.uri().toString()).isEqualTo("https://example.com/api/payments");
        }

        @Test
        @DisplayName("should handle request with body")
        void shouldHandleRequestWithBody() {
            LoggingRequestInterceptor loggingWithBody = new LoggingRequestInterceptor(Level.INFO, true, true);

            HttpRequest request = HttpRequest.builder().method(HttpMethod.POST)
                    .uri(URI.create("https://example.com/api")).body("{\"amount\":\"10.00\"}").build();

            HttpRequest result = loggingWithBody.intercept(request);

            assertThat(result).isSameAs(request);
            assertThat(result.getBody()).isPresent();
        }

        @Test
        @DisplayName("should handle sensitive headers")
        void shouldHandleSensitiveHeaders() {
            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri(URI.create("https://example.com/api"))
                    .headers(Map.of("Authorization", "Bearer secret-token", "Content-Type", "application/json"))
                    .build();

            HttpRequest result = interceptor.intercept(request);

            // Should not modify request even with sensitive headers
            assertThat(result).isSameAs(request);
            assertThat(result.headers()).containsEntry("Authorization", "Bearer secret-token");
        }

        @Test
        @DisplayName("should handle request without headers")
        void shouldHandleRequestWithoutHeaders() {
            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri(URI.create("https://example.com/api")).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result).isSameAs(request);
            assertThat(result.headers()).isEmpty();
        }

        @Test
        @DisplayName("should handle request without body")
        void shouldHandleRequestWithoutBody() {
            HttpRequest request = HttpRequest.builder().method(HttpMethod.DELETE)
                    .uri(URI.create("https://example.com/api/resource/123")).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result).isSameAs(request);
            assertThat(result.getBody()).isEmpty();
        }

        @Test
        @DisplayName("should log different HTTP methods")
        void shouldLogDifferentHttpMethods() {
            for (HttpMethod method : HttpMethod.values()) {
                HttpRequest request = HttpRequest.builder().method(method).uri(URI.create("https://example.com/api"))
                        .build();

                HttpRequest result = interceptor.intercept(request);

                assertThat(result).isSameAs(request);
            }
        }
    }

    @Nested
    @DisplayName("Configuration")
    class ConfigurationTests {

        @Test
        @DisplayName("should support headers-only logging")
        void shouldSupportHeadersOnlyLogging() {
            LoggingRequestInterceptor headersOnly = new LoggingRequestInterceptor(Level.INFO, true, false);

            HttpRequest request = HttpRequest.builder().method(HttpMethod.POST)
                    .uri(URI.create("https://example.com/api")).headers(Map.of("Content-Type", "application/json"))
                    .body("{\"test\":\"data\"}").build();

            HttpRequest result = headersOnly.intercept(request);

            assertThat(result).isSameAs(request);
        }

        @Test
        @DisplayName("should support no headers or body logging")
        void shouldSupportNoHeadersOrBodyLogging() {
            LoggingRequestInterceptor minimal = new LoggingRequestInterceptor(Level.INFO, false, false);

            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri(URI.create("https://example.com/api")).headers(Map.of("Content-Type", "application/json"))
                    .build();

            HttpRequest result = minimal.intercept(request);

            assertThat(result).isSameAs(request);
        }

        @Test
        @DisplayName("should support full logging")
        void shouldSupportFullLogging() {
            LoggingRequestInterceptor full = new LoggingRequestInterceptor(Level.FINE, true, true);

            HttpRequest request = HttpRequest.builder().method(HttpMethod.POST)
                    .uri(URI.create("https://example.com/api")).headers(Map.of("Content-Type", "application/json"))
                    .body("{\"full\":\"logging\"}").build();

            HttpRequest result = full.intercept(request);

            assertThat(result).isSameAs(request);
        }
    }
}
