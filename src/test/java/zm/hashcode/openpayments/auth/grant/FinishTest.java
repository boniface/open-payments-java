package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link Finish}.
 */
@DisplayName("Finish")
class FinishTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should create with all fields")
        void shouldCreateWithAllFields() {
            Finish finish = new Finish("redirect", "https://example.com/callback", "nonce-12345");

            assertThat(finish.method()).isEqualTo("redirect");
            assertThat(finish.uri()).isEqualTo("https://example.com/callback");
            assertThat(finish.nonce()).isEqualTo("nonce-12345");
        }

        @Test
        @DisplayName("should throw when method is null")
        void shouldThrowWhenMethodIsNull() {
            assertThatThrownBy(() -> new Finish(null, "https://example.com/callback", "nonce"))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("method must not be null");
        }

        @Test
        @DisplayName("should throw when uri is null")
        void shouldThrowWhenUriIsNull() {
            assertThatThrownBy(() -> new Finish("redirect", null, "nonce")).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("uri must not be null");
        }

        @Test
        @DisplayName("should throw when nonce is null")
        void shouldThrowWhenNonceIsNull() {
            assertThatThrownBy(() -> new Finish("redirect", "https://example.com/callback", null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("nonce must not be null");
        }
    }

    @Nested
    @DisplayName("JSON Serialization")
    class JsonSerializationTests {

        private final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jdk8.Jdk8Module());

        @Test
        @DisplayName("should serialize to JSON")
        void shouldSerializeToJson() throws Exception {
            Finish finish = new Finish("redirect", "https://example.com/callback", "test-nonce");

            String json = objectMapper.writeValueAsString(finish);

            assertThat(json).contains("\"method\"");
            assertThat(json).contains("\"redirect\"");
            assertThat(json).contains("\"uri\"");
            assertThat(json).contains("https://example.com/callback");
            assertThat(json).contains("\"nonce\"");
            assertThat(json).contains("test-nonce");
        }

        @Test
        @DisplayName("should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            String json = """
                    {
                        "method": "redirect",
                        "uri": "https://example.com/callback",
                        "nonce": "my-nonce"
                    }
                    """;

            Finish finish = objectMapper.readValue(json, Finish.class);

            assertThat(finish.method()).isEqualTo("redirect");
            assertThat(finish.uri()).isEqualTo("https://example.com/callback");
            assertThat(finish.nonce()).isEqualTo("my-nonce");
        }

        @Test
        @DisplayName("should round-trip through JSON")
        void shouldRoundTripThroughJson() throws Exception {
            Finish original = new Finish("redirect", "https://test.com/finish", "nonce-xyz");

            String json = objectMapper.writeValueAsString(original);
            Finish deserialized = objectMapper.readValue(json, Finish.class);

            assertThat(deserialized).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Finish Methods")
    class FinishMethodsTests {

        @Test
        @DisplayName("should support redirect method")
        void shouldSupportRedirectMethod() {
            Finish finish = new Finish("redirect", "https://example.com/callback", "nonce");

            assertThat(finish.method()).isEqualTo("redirect");
        }

        @Test
        @DisplayName("should support push method")
        void shouldSupportPushMethod() {
            Finish finish = new Finish("push", "https://example.com/callback", "nonce");

            assertThat(finish.method()).isEqualTo("push");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle long nonce")
        void shouldHandleLongNonce() {
            String longNonce = "n".repeat(1000);

            Finish finish = new Finish("redirect", "https://example.com/callback", longNonce);

            assertThat(finish.nonce()).hasSize(1000);
        }

        @Test
        @DisplayName("should handle complex URIs")
        void shouldHandleComplexUris() {
            String complexUri = "https://example.com/callback?param1=value1&param2=value2#fragment";

            Finish finish = new Finish("redirect", complexUri, "nonce");

            assertThat(finish.uri()).isEqualTo(complexUri);
        }
    }
}
