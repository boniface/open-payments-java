package zm.hashcode.openpayments.http.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import zm.hashcode.openpayments.http.core.HttpMethod;
import zm.hashcode.openpayments.http.core.HttpRequest;

/**
 * Unit tests for {@link AuthenticationInterceptor}.
 */
@DisplayName("AuthenticationInterceptor")
class AuthenticationInterceptorTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should create bearer token interceptor")
        void shouldCreateBearerInterceptor() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.bearer("my-token");

            assertThat(interceptor).isNotNull();
            assertThat(interceptor.getAuthorizationHeaderValue()).isEqualTo("Bearer my-token");
        }

        @Test
        @DisplayName("should create GNAP token interceptor")
        void shouldCreateGnapInterceptor() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.gnap("gnap-token");

            assertThat(interceptor).isNotNull();
            assertThat(interceptor.getAuthorizationHeaderValue()).isEqualTo("GNAP gnap-token");
        }

        @Test
        @DisplayName("should create basic auth interceptor")
        void shouldCreateBasicAuthInterceptor() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.basic("dXNlcjpwYXNz");

            assertThat(interceptor).isNotNull();
            assertThat(interceptor.getAuthorizationHeaderValue()).isEqualTo("Basic dXNlcjpwYXNz");
        }

        @Test
        @DisplayName("should create custom auth interceptor")
        void shouldCreateCustomAuthInterceptor() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.custom("Custom", "my-credentials");

            assertThat(interceptor).isNotNull();
            assertThat(interceptor.getAuthorizationHeaderValue()).isEqualTo("Custom my-credentials");
        }

        @Test
        @DisplayName("should throw when bearer token is null")
        void shouldThrowWhenBearerTokenIsNull() {
            assertThatThrownBy(() -> AuthenticationInterceptor.bearer(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("token must not be null");
        }

        @Test
        @DisplayName("should throw when GNAP token is null")
        void shouldThrowWhenGnapTokenIsNull() {
            assertThatThrownBy(() -> AuthenticationInterceptor.gnap(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("token must not be null");
        }

        @Test
        @DisplayName("should throw when basic credentials are null")
        void shouldThrowWhenBasicCredentialsAreNull() {
            assertThatThrownBy(() -> AuthenticationInterceptor.basic(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("credentials must not be null");
        }

        @Test
        @DisplayName("should throw when custom scheme is null")
        void shouldThrowWhenCustomSchemeIsNull() {
            assertThatThrownBy(() -> AuthenticationInterceptor.custom(null, "credentials"))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("scheme must not be null");
        }

        @Test
        @DisplayName("should throw when custom credentials are null")
        void shouldThrowWhenCustomCredentialsAreNull() {
            assertThatThrownBy(() -> AuthenticationInterceptor.custom("Custom", null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("credentials must not be null");
        }
    }

    @Nested
    @DisplayName("Request Interception")
    class RequestInterceptionTests {

        @Test
        @DisplayName("should add Authorization header with bearer token")
        void shouldAddBearerAuthorizationHeader() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.bearer("access-token-123");

            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri(URI.create("https://api.example.com/resource")).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result.headers()).containsEntry("Authorization", "Bearer access-token-123");
        }

        @Test
        @DisplayName("should add Authorization header with GNAP token")
        void shouldAddGnapAuthorizationHeader() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.gnap("gnap-token-456");

            HttpRequest request = HttpRequest.builder().method(HttpMethod.POST)
                    .uri(URI.create("https://auth.example.com/token")).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result.headers()).containsEntry("Authorization", "GNAP gnap-token-456");
        }

        @Test
        @DisplayName("should preserve existing headers")
        void shouldPreserveExistingHeaders() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.bearer("token");

            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri(URI.create("https://api.example.com"))
                    .headers(Map.of("Content-Type", "application/json", "Accept", "application/json")).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result.headers()).containsEntry("Content-Type", "application/json");
            assertThat(result.headers()).containsEntry("Accept", "application/json");
            assertThat(result.headers()).containsEntry("Authorization", "Bearer token");
        }

        @Test
        @DisplayName("should override existing Authorization header")
        void shouldOverrideExistingAuthorizationHeader() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.bearer("new-token");

            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri(URI.create("https://api.example.com")).headers(Map.of("Authorization", "Bearer old-token"))
                    .build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result.headers()).containsEntry("Authorization", "Bearer new-token");
        }

        @Test
        @DisplayName("should preserve request method and URI")
        void shouldPreserveRequestMethodAndUri() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.gnap("token");

            HttpRequest request = HttpRequest.builder().method(HttpMethod.POST)
                    .uri(URI.create("https://payments.example.com/incoming")).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result.method()).isEqualTo(HttpMethod.POST);
            assertThat(result.uri().toString()).isEqualTo("https://payments.example.com/incoming");
        }

        @Test
        @DisplayName("should preserve request body")
        void shouldPreserveRequestBody() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.bearer("token");

            String requestBody = "{\"amount\":\"100.00\"}";
            HttpRequest request = HttpRequest.builder().method(HttpMethod.POST)
                    .uri(URI.create("https://api.example.com/payments")).body(requestBody).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result.getBody()).hasValue(requestBody);
        }

        @Test
        @DisplayName("should handle request with empty headers")
        void shouldHandleRequestWithEmptyHeaders() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.bearer("token");

            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri(URI.create("https://api.example.com")).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result.headers()).hasSize(1);
            assertThat(result.headers()).containsEntry("Authorization", "Bearer token");
        }
    }

    @Nested
    @DisplayName("Authentication Schemes")
    class AuthenticationSchemesTests {

        @Test
        @DisplayName("should support OAuth 2.0 bearer tokens")
        void shouldSupportOAuth2BearerTokens() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor
                    .bearer("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");

            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri(URI.create("https://api.example.com")).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result.headers().get("Authorization")).startsWith("Bearer ")
                    .contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        }

        @Test
        @DisplayName("should support Open Payments GNAP tokens")
        void shouldSupportOpenPaymentsGnapTokens() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor
                    .gnap("OS9M2PMHKUR64TB8N6BW7OZB8CDFONP219RP1LT0");

            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri(URI.create("https://auth.example.com")).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result.headers().get("Authorization"))
                    .isEqualTo("GNAP OS9M2PMHKUR64TB8N6BW7OZB8CDFONP219RP1LT0");
        }

        @Test
        @DisplayName("should support HTTP Basic authentication")
        void shouldSupportHttpBasicAuthentication() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.basic("dXNlcm5hbWU6cGFzc3dvcmQ=");

            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri(URI.create("https://api.example.com")).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result.headers().get("Authorization")).isEqualTo("Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
        }

        @Test
        @DisplayName("should support custom authentication schemes")
        void shouldSupportCustomAuthenticationSchemes() {
            AuthenticationInterceptor interceptor = AuthenticationInterceptor.custom("ApiKey", "sk_live_1234567890");

            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri(URI.create("https://api.example.com")).build();

            HttpRequest result = interceptor.intercept(request);

            assertThat(result.headers().get("Authorization")).isEqualTo("ApiKey sk_live_1234567890");
        }
    }
}
