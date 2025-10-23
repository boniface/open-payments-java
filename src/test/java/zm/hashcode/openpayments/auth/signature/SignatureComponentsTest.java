package zm.hashcode.openpayments.auth.signature;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link SignatureComponents}.
 */
@DisplayName("SignatureComponents")
class SignatureComponentsTest {

    private static final String BASE_URI = "https://example.com";

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should build with required fields")
        void shouldBuildWithRequiredFields() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI + "/grant").build();

            assertThat(components.getMethod()).isEqualTo("POST");
            assertThat(components.getTargetUri()).isEqualTo(BASE_URI + "/grant");
            assertThat(components.getHeaders()).isEmpty();
            assertThat(components.hasBody()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("should throw when method is blank")
        void shouldThrowWhenMethodIsBlank(String method) {
            var builder = SignatureComponents.builder().method(method).targetUri(BASE_URI);

            assertThatThrownBy(builder::build).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("method must not be blank");
        }

        @Test
        @DisplayName("should throw when method is null")
        void shouldThrowWhenMethodIsNull() {
            var builder = SignatureComponents.builder().targetUri(BASE_URI);

            assertThatThrownBy(builder::build).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("method must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"   "})
        @DisplayName("should throw when targetUri is blank")
        void shouldThrowWhenTargetUriIsBlank(String uri) {
            var builder = SignatureComponents.builder().method("GET").targetUri(uri);

            assertThatThrownBy(builder::build).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("targetUri must not be blank");
        }

        @Test
        @DisplayName("should throw when targetUri is null")
        void shouldThrowWhenTargetUriIsNull() {
            var builder = SignatureComponents.builder().method("GET");

            assertThatThrownBy(builder::build).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("targetUri must not be null");
        }
    }

    @Nested
    @DisplayName("Header Management")
    class HeaderManagementTests {

        @Test
        @DisplayName("should add and retrieve headers")
        void shouldAddAndRetrieveHeaders() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI)
                    .addHeader("content-type", "application/json").addHeader("content-length", "123").build();

            assertThat(components.getHeader("content-type")).contains("application/json");
            assertThat(components.getHeader("content-length")).contains("123");
            assertThat(components.getHeaders()).hasSize(2);
        }

        @ParameterizedTest
        @ValueSource(strings = {"content-type", "Content-Type", "CONTENT-TYPE"})
        @DisplayName("should handle case-insensitive headers")
        void shouldHandleCaseInsensitiveHeaders(String headerName) {
            var components = SignatureComponents.builder().method("GET").targetUri(BASE_URI)
                    .addHeader("Content-Type", "application/json").build();

            assertThat(components.getHeader(headerName)).contains("application/json");
        }

        @Test
        @DisplayName("should add multiple headers at once")
        void shouldAddMultipleHeadersAtOnce() {
            Map<String, String> headers = Map.of("content-type", "application/json", "content-length", "123",
                    "authorization", "GNAP token-value");

            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI).headers(headers).build();

            assertThat(components.getHeaders()).hasSize(3);
            assertThat(components.getHeader("content-type")).contains("application/json");
        }

        @Test
        @DisplayName("should normalize header names to lowercase")
        void shouldNormalizeHeaderNames() {
            var components = SignatureComponents.builder().method("GET").targetUri(BASE_URI)
                    .addHeader("Content-Type", "application/json").addHeader("AUTHORIZATION", "token").build();

            Map<String, String> headers = components.getHeaders();
            assertThat(headers).containsKey("content-type");
            assertThat(headers).containsKey("authorization");
        }

        @Test
        @DisplayName("should return immutable headers map")
        void shouldReturnImmutableHeadersMap() {
            var components = SignatureComponents.builder().method("GET").targetUri(BASE_URI).addHeader("test", "value")
                    .build();

            assertThatThrownBy(() -> components.getHeaders().put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @ParameterizedTest
        @MethodSource("nullHeaderProvider")
        @DisplayName("should throw when adding null header")
        void shouldThrowWhenAddingNullHeader(String name, String value, String expectedMessage) {
            var builder = SignatureComponents.builder().method("GET").targetUri(BASE_URI);

            assertThatThrownBy(() -> builder.addHeader(name, value)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining(expectedMessage);
        }

        static Stream<Arguments> nullHeaderProvider() {
            return Stream.of(Arguments.of(null, "value", "name must not be null"),
                    Arguments.of("content-type", null, "value must not be null"));
        }

        @Test
        @DisplayName("should handle headers with special characters")
        void shouldHandleHeadersWithSpecialCharacters() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI)
                    .addHeader("content-type", "application/json; charset=utf-8")
                    .addHeader("custom-header", "value with spaces").build();

            assertThat(components.getHeader("content-type")).contains("application/json; charset=utf-8");
            assertThat(components.getHeader("custom-header")).contains("value with spaces");
        }
    }

    @Nested
    @DisplayName("Body Management")
    class BodyManagementTests {

        @Test
        @DisplayName("should handle body presence")
        void shouldHandleBodyPresence() {
            var withBody = SignatureComponents.builder().method("POST").targetUri(BASE_URI).body("{\"test\":\"value\"}")
                    .build();

            assertThat(withBody.hasBody()).isTrue();
            assertThat(withBody.getBody()).contains("{\"test\":\"value\"}");
        }

        @Test
        @DisplayName("should handle null body")
        void shouldHandleNullBody() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI).body(null).build();

            assertThat(components.hasBody()).isFalse();
            assertThat(components.getBody()).isEmpty();
        }

        @Test
        @DisplayName("should handle empty string body")
        void shouldHandleEmptyStringBody() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI).body("").build();

            assertThat(components.hasBody()).isTrue();
            assertThat(components.getBody()).contains("");
        }

        @ParameterizedTest
        @MethodSource("bodyVariationsProvider")
        @DisplayName("should handle various body types")
        void shouldHandleVariousBodyTypes(String body) {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI).body(body).build();

            assertThat(components.hasBody()).isTrue();
            assertThat(components.getBody()).contains(body);
        }

        static Stream<Arguments> bodyVariationsProvider() {
            return Stream.of(Arguments.of("x".repeat(10000)), // Large body
                    Arguments.of("{\n  \"key\": \"value\"\n}"), // Newlines
                    Arguments.of("{\"message\":\"Hello 世界 🌍\"}") // Unicode
            );
        }
    }

    @Nested
    @DisplayName("Component Identifiers")
    class ComponentIdentifierTests {

        @Test
        @DisplayName("should include method and target URI by default")
        void shouldIncludeMethodAndTargetUriByDefault() {
            var components = SignatureComponents.builder().method("GET").targetUri(BASE_URI).build();

            assertThat(components.getComponentIdentifiers()).hasSize(2).containsExactly("@method", "@target-uri");
        }

        @Test
        @DisplayName("should include authorization when present")
        void shouldIncludeAuthorizationWhenPresent() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI)
                    .addHeader("authorization", "GNAP token").build();

            assertThat(components.getComponentIdentifiers()).contains("authorization").startsWith("@method",
                    "@target-uri", "authorization");
        }

        @Test
        @DisplayName("should include content-digest only when body present")
        void shouldIncludeContentDigestOnlyWithBody() {
            var withBody = SignatureComponents.builder().method("POST").targetUri(BASE_URI)
                    .addHeader("content-digest", "sha-256=:abc:=").body("{}").build();

            assertThat(withBody.getComponentIdentifiers()).contains("content-digest");

            var withoutBody = SignatureComponents.builder().method("POST").targetUri(BASE_URI)
                    .addHeader("content-digest", "sha-256=:abc:=").build();

            assertThat(withoutBody.getComponentIdentifiers()).doesNotContain("content-digest");
        }

        @Test
        @DisplayName("should follow Open Payments ordering")
        void shouldFollowOpenPaymentsOrdering() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI + "/grant")
                    .addHeader("content-type", "application/json").addHeader("content-length", "123")
                    .addHeader("authorization", "GNAP token").addHeader("content-digest", "sha-256=:abc:=").body("{}")
                    .build();

            assertThat(components.getComponentIdentifiers()).containsExactly("@method", "@target-uri", "authorization",
                    "content-digest", "content-type", "content-length");
        }

        @ParameterizedTest
        @ValueSource(strings = {"content-type", "content-length"})
        @DisplayName("should include optional headers when present")
        void shouldIncludeOptionalHeadersWhenPresent(String headerName) {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI)
                    .addHeader(headerName, "value").build();

            assertThat(components.getComponentIdentifiers()).contains(headerName);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @ParameterizedTest
        @MethodSource("complexUriProvider")
        @DisplayName("should handle complex URIs")
        void shouldHandleComplexUris(String uri) {
            var components = SignatureComponents.builder().method("GET").targetUri(uri).build();

            assertThat(components.getTargetUri()).isEqualTo(uri);
        }

        static Stream<Arguments> complexUriProvider() {
            return Stream.of(Arguments.of("https://auth.example.com:8443/grant?client_id=123&state=abc#fragment"),
                    Arguments.of("https://example.com/path?name=Test%20User&symbol=%24"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"})
        @DisplayName("should handle all HTTP methods")
        void shouldHandleAllHttpMethods(String method) {
            var components = SignatureComponents.builder().method(method).targetUri(BASE_URI).build();

            assertThat(components.getMethod()).isEqualTo(method);
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should support fluent builder")
        void shouldSupportFluentBuilder() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI)
                    .addHeader("content-type", "application/json").addHeader("authorization", "token").body("{}")
                    .build();

            assertThat(components).isNotNull();
            assertThat(components.getMethod()).isEqualTo("POST");
            assertThat(components.getHeaders()).hasSize(2);
            assertThat(components.hasBody()).isTrue();
        }

        @Test
        @DisplayName("should allow multiple build calls")
        void shouldAllowMultipleBuildCalls() {
            var builder = SignatureComponents.builder().method("GET").targetUri(BASE_URI);

            var components1 = builder.build();
            var components2 = builder.build();

            assertThat(components1.getMethod()).isEqualTo(components2.getMethod());
            assertThat(components1.getTargetUri()).isEqualTo(components2.getTargetUri());
        }

        @Test
        @DisplayName("should throw when headers map is null")
        void shouldThrowWhenHeadersMapIsNull() {
            var builder = SignatureComponents.builder().method("GET").targetUri(BASE_URI);

            assertThatThrownBy(() -> builder.headers(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("headers must not be null");
        }

        @Test
        @DisplayName("should throw when getHeader name is null")
        void shouldThrowWhenGetHeaderNameIsNull() {
            var components = SignatureComponents.builder().method("GET").targetUri(BASE_URI).build();

            assertThatThrownBy(() -> components.getHeader(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("name must not be null");
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should have readable toString")
        void shouldHaveReadableToString() {
            var components = SignatureComponents.builder().method("POST").targetUri(BASE_URI + "/grant")
                    .addHeader("content-type", "application/json").body("{}").build();

            String toString = components.toString();

            assertThat(toString).contains("SignatureComponents").contains("method='POST'")
                    .contains("targetUri='https://example.com/grant'").contains("headers=1").contains("hasBody=true");
        }

        @Test
        @DisplayName("should show correct header count in toString")
        void shouldShowCorrectHeaderCountInToString() {
            var components = SignatureComponents.builder().method("GET").targetUri(BASE_URI)
                    .addHeader("header1", "value1").addHeader("header2", "value2").addHeader("header3", "value3")
                    .build();

            assertThat(components.toString()).contains("headers=3");
        }
    }
}
