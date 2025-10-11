package zm.hashcode.openpayments.auth.signature;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SignatureComponents}.
 */
class SignatureComponentsTest {

    // ========================================
    // Construction Tests
    // ========================================

    @Test
    void shouldBuildWithRequiredFields() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com/grant").build();

        assertThat(components.getMethod()).isEqualTo("POST");
        assertThat(components.getTargetUri()).isEqualTo("https://example.com/grant");
        assertThat(components.getHeaders()).isEmpty();
        assertThat(components.hasBody()).isFalse();
    }

    @Test
    void shouldThrowWhenMethodIsNull() {
        var builder = SignatureComponents.builder().targetUri("https://example.com");

        assertThatThrownBy(() -> builder.build()).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("method must not be null");
    }

    @Test
    void shouldThrowWhenMethodIsBlank() {
        var builder = SignatureComponents.builder().method("").targetUri("https://example.com");

        assertThatThrownBy(() -> builder.build()).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("method must not be blank");
    }

    @Test
    void shouldThrowWhenMethodIsWhitespace() {
        var builder = SignatureComponents.builder().method("   ").targetUri("https://example.com");

        assertThatThrownBy(() -> builder.build()).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("method must not be blank");
    }

    @Test
    void shouldThrowWhenTargetUriIsNull() {
        var builder = SignatureComponents.builder().method("GET");

        assertThatThrownBy(() -> builder.build()).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("targetUri must not be null");
    }

    @Test
    void shouldThrowWhenTargetUriIsBlank() {
        var builder = SignatureComponents.builder().method("GET").targetUri("   ");

        assertThatThrownBy(() -> builder.build()).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("targetUri must not be blank");
    }

    // ========================================
    // Header Management Tests
    // ========================================

    @Test
    void shouldAddAndRetrieveHeaders() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-type", "application/json").addHeader("content-length", "123").build();

        assertThat(components.getHeader("content-type")).contains("application/json");
        assertThat(components.getHeader("content-length")).contains("123");
        assertThat(components.getHeaders()).hasSize(2);
    }

    @Test
    void shouldHandleCaseInsensitiveHeaders() {
        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com")
                .addHeader("Content-Type", "application/json").build();

        // RFC 9421: header names are case-insensitive
        assertThat(components.getHeader("content-type")).contains("application/json");
        assertThat(components.getHeader("CONTENT-TYPE")).contains("application/json");
        assertThat(components.getHeader("Content-Type")).contains("application/json");
    }

    @Test
    void shouldReturnEmptyForMissingHeader() {
        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        assertThat(components.getHeader("authorization")).isEmpty();
        assertThat(components.getHeader("content-type")).isEmpty();
    }

    @Test
    void shouldAddMultipleHeadersAtOnce() {
        Map<String, String> headers = Map.of("content-type", "application/json", "content-length", "123",
                "authorization", "GNAP token-value");

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com").headers(headers)
                .build();

        assertThat(components.getHeaders()).hasSize(3);
        assertThat(components.getHeader("content-type")).contains("application/json");
        assertThat(components.getHeader("content-length")).contains("123");
        assertThat(components.getHeader("authorization")).contains("GNAP token-value");
    }

    @Test
    void shouldReturnImmutableHeadersMap() {
        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com")
                .addHeader("test", "value").build();

        assertThatThrownBy(() -> components.getHeaders().put("new", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowWhenAddingNullHeaderName() {
        var builder = SignatureComponents.builder().method("GET").targetUri("https://example.com");

        assertThatThrownBy(() -> builder.addHeader(null, "value")).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name must not be null");
    }

    @Test
    void shouldThrowWhenAddingNullHeaderValue() {
        var builder = SignatureComponents.builder().method("GET").targetUri("https://example.com");

        assertThatThrownBy(() -> builder.addHeader("content-type", null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("value must not be null");
    }

    @Test
    void shouldNormalizeHeaderNamesToLowercase() {
        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com")
                .addHeader("Content-Type", "application/json").addHeader("AUTHORIZATION", "token").build();

        // Headers are stored in lowercase
        Map<String, String> headers = components.getHeaders();
        assertThat(headers).containsKey("content-type");
        assertThat(headers).containsKey("authorization");
    }

    // ========================================
    // Body Management Tests
    // ========================================

    @Test
    void shouldHandleBodyPresence() {
        var withBody = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .body("{\"test\":\"value\"}").build();

        assertThat(withBody.hasBody()).isTrue();
        assertThat(withBody.getBody()).contains("{\"test\":\"value\"}");

        var withoutBody = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        assertThat(withoutBody.hasBody()).isFalse();
        assertThat(withoutBody.getBody()).isEmpty();
    }

    @Test
    void shouldHandleNullBody() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com").body(null)
                .build();

        assertThat(components.hasBody()).isFalse();
        assertThat(components.getBody()).isEmpty();
    }

    @Test
    void shouldHandleEmptyStringBody() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com").body("").build();

        assertThat(components.hasBody()).isTrue();
        assertThat(components.getBody()).contains("");
    }

    @Test
    void shouldHandleLargeBody() {
        String largeBody = "x".repeat(10000);

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com").body(largeBody)
                .build();

        assertThat(components.hasBody()).isTrue();
        assertThat(components.getBody()).contains(largeBody);
    }

    // ========================================
    // Component Identifier Ordering Tests
    // ========================================

    @Test
    void shouldIncludeMethodAndTargetUriByDefault() {
        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        List<String> identifiers = components.getComponentIdentifiers();

        assertThat(identifiers).hasSize(2).containsExactly("@method", "@target-uri");
    }

    @Test
    void shouldIncludeAuthorizationWhenPresent() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("authorization", "GNAP token").build();

        List<String> identifiers = components.getComponentIdentifiers();

        assertThat(identifiers).contains("authorization").startsWith("@method", "@target-uri", "authorization");
    }

    @Test
    void shouldIncludeContentDigestWhenBodyPresent() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-digest", "sha-256=:abc:=").body("{}").build();

        List<String> identifiers = components.getComponentIdentifiers();

        assertThat(identifiers).contains("content-digest");
    }

    @Test
    void shouldNotIncludeContentDigestWithoutBody() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-digest", "sha-256=:abc:=")
                // No body
                .build();

        List<String> identifiers = components.getComponentIdentifiers();

        assertThat(identifiers).doesNotContain("content-digest");
    }

    @Test
    void shouldNotIncludeContentDigestWithoutHeader() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                // No content-digest header
                .body("{}").build();

        List<String> identifiers = components.getComponentIdentifiers();

        assertThat(identifiers).doesNotContain("content-digest");
    }

    @Test
    void shouldFollowOpenPaymentsOrdering() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com/grant")
                .addHeader("content-type", "application/json").addHeader("content-length", "123")
                .addHeader("authorization", "GNAP token").addHeader("content-digest", "sha-256=:abc:=").body("{}")
                .build();

        List<String> identifiers = components.getComponentIdentifiers();

        // Order: @method, @target-uri, authorization, content-digest, content-type,
        // content-length
        assertThat(identifiers).containsExactly("@method", "@target-uri", "authorization", "content-digest",
                "content-type", "content-length");
    }

    @Test
    void shouldHandlePartialHeaders() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-type", "application/json")
                // No content-length, no authorization
                .build();

        List<String> identifiers = components.getComponentIdentifiers();

        assertThat(identifiers).containsExactly("@method", "@target-uri", "content-type");
    }

    @Test
    void shouldIncludeContentTypeWhenPresent() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-type", "application/json").build();

        List<String> identifiers = components.getComponentIdentifiers();

        assertThat(identifiers).contains("content-type");
    }

    @Test
    void shouldIncludeContentLengthWhenPresent() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-length", "123").build();

        List<String> identifiers = components.getComponentIdentifiers();

        assertThat(identifiers).contains("content-length");
    }

    @Test
    void shouldNotIncludeMissingOptionalHeaders() {
        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        List<String> identifiers = components.getComponentIdentifiers();

        assertThat(identifiers).doesNotContain("authorization", "content-digest", "content-type", "content-length");
    }

    // ========================================
    // Edge Cases
    // ========================================

    @Test
    void shouldHandleEmptyHeaders() {
        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        assertThat(components.getHeaders()).isEmpty();
        assertThat(components.getComponentIdentifiers()).hasSize(2);
    }

    @Test
    void shouldHandleComplexUri() {
        String complexUri = "https://auth.example.com:8443/grant?client_id=123&state=abc#fragment";

        var components = SignatureComponents.builder().method("POST").targetUri(complexUri).build();

        assertThat(components.getTargetUri()).isEqualTo(complexUri);
    }

    @Test
    void shouldHandleUriWithSpecialCharacters() {
        String uriWithSpecialChars = "https://example.com/path?name=Test%20User&symbol=%24";

        var components = SignatureComponents.builder().method("GET").targetUri(uriWithSpecialChars).build();

        assertThat(components.getTargetUri()).isEqualTo(uriWithSpecialChars);
    }

    @Test
    void shouldHandleAllHttpMethods() {
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};

        for (String method : methods) {
            var components = SignatureComponents.builder().method(method).targetUri("https://example.com").build();

            assertThat(components.getMethod()).isEqualTo(method);
        }
    }

    @Test
    void shouldHandleHeadersWithSpecialCharacters() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-type", "application/json; charset=utf-8")
                .addHeader("custom-header", "value with spaces").build();

        assertThat(components.getHeader("content-type")).contains("application/json; charset=utf-8");
        assertThat(components.getHeader("custom-header")).contains("value with spaces");
    }

    @Test
    void shouldHandleBodyWithNewlines() {
        String bodyWithNewlines = "{\n  \"key\": \"value\"\n}";

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .body(bodyWithNewlines).build();

        assertThat(components.getBody()).contains(bodyWithNewlines);
    }

    @Test
    void shouldHandleBodyWithUnicodeCharacters() {
        String unicodeBody = "{\"message\":\"Hello 世界 🌍\"}";

        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com").body(unicodeBody)
                .build();

        assertThat(components.getBody()).contains(unicodeBody);
    }

    // ========================================
    // toString Tests
    // ========================================

    @Test
    void shouldHaveReadableToString() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com/grant")
                .addHeader("content-type", "application/json").body("{}").build();

        String toString = components.toString();

        assertThat(toString).contains("SignatureComponents").contains("method='POST'")
                .contains("targetUri='https://example.com/grant'").contains("headers=1").contains("hasBody=true");
    }

    @Test
    void shouldShowCorrectHeaderCountInToString() {
        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com")
                .addHeader("header1", "value1").addHeader("header2", "value2").addHeader("header3", "value3").build();

        String toString = components.toString();

        assertThat(toString).contains("headers=3");
    }

    // ========================================
    // Builder Tests
    // ========================================

    @Test
    void shouldSupportFluentBuilder() {
        var components = SignatureComponents.builder().method("POST").targetUri("https://example.com")
                .addHeader("content-type", "application/json").addHeader("authorization", "token").body("{}").build();

        assertThat(components).isNotNull();
        assertThat(components.getMethod()).isEqualTo("POST");
        assertThat(components.getHeaders()).hasSize(2);
        assertThat(components.hasBody()).isTrue();
    }

    @Test
    void shouldAllowMultipleBuildCalls() {
        var builder = SignatureComponents.builder().method("GET").targetUri("https://example.com");

        var components1 = builder.build();
        var components2 = builder.build();

        assertThat(components1).isNotNull();
        assertThat(components2).isNotNull();
        // Both should be equal since builder state hasn't changed
        assertThat(components1.getMethod()).isEqualTo(components2.getMethod());
        assertThat(components1.getTargetUri()).isEqualTo(components2.getTargetUri());
    }

    @Test
    void shouldThrowWhenHeadersMapIsNull() {
        var builder = SignatureComponents.builder().method("GET").targetUri("https://example.com");

        assertThatThrownBy(() -> builder.headers(null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("headers must not be null");
    }

    @Test
    void shouldThrowWhenGetHeaderNameIsNull() {
        var components = SignatureComponents.builder().method("GET").targetUri("https://example.com").build();

        assertThatThrownBy(() -> components.getHeader(null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name must not be null");
    }
}
