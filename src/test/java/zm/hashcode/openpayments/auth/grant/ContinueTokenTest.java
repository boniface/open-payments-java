package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link ContinueToken}.
 */
@DisplayName("ContinueToken")
class ContinueTokenTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should create with value")
        void shouldCreateWithValue() {
            ContinueToken token = new ContinueToken("my-token-value");

            assertThat(token.value()).isEqualTo("my-token-value");
        }

        @Test
        @DisplayName("should throw when value is null")
        void shouldThrowWhenValueIsNull() {
            assertThatThrownBy(() -> new ContinueToken(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("value must not be null");
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
            ContinueToken token = new ContinueToken("token-abc-123");

            String json = objectMapper.writeValueAsString(token);

            assertThat(json).contains("\"value\"");
            assertThat(json).contains("token-abc-123");
        }

        @Test
        @DisplayName("should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            String json = """
                    {
                        "value": "my-continue-token"
                    }
                    """;

            ContinueToken token = objectMapper.readValue(json, ContinueToken.class);

            assertThat(token.value()).isEqualTo("my-continue-token");
        }

        @Test
        @DisplayName("should round-trip through JSON")
        void shouldRoundTripThroughJson() throws Exception {
            ContinueToken original = new ContinueToken("test-token-xyz");

            String json = objectMapper.writeValueAsString(original);
            ContinueToken deserialized = objectMapper.readValue(json, ContinueToken.class);

            assertThat(deserialized).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Equality")
    class EqualityTests {

        @Test
        @DisplayName("should be equal with same value")
        void shouldBeEqualWithSameValue() {
            ContinueToken token1 = new ContinueToken("same-token");
            ContinueToken token2 = new ContinueToken("same-token");

            assertThat(token1).isEqualTo(token2);
        }

        @Test
        @DisplayName("should not be equal with different values")
        void shouldNotBeEqualWithDifferentValues() {
            ContinueToken token1 = new ContinueToken("token-1");
            ContinueToken token2 = new ContinueToken("token-2");

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("should have same hashCode with same value")
        void shouldHaveSameHashCodeWithSameValue() {
            ContinueToken token1 = new ContinueToken("token");
            ContinueToken token2 = new ContinueToken("token");

            assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty string")
        void shouldHandleEmptyString() {
            ContinueToken token = new ContinueToken("");

            assertThat(token.value()).isEmpty();
        }

        @Test
        @DisplayName("should handle long token")
        void shouldHandleLongToken() {
            String longToken = "t".repeat(1000);
            ContinueToken token = new ContinueToken(longToken);

            assertThat(token.value()).hasSize(1000);
        }

        @Test
        @DisplayName("should handle special characters")
        void shouldHandleSpecialCharacters() {
            ContinueToken token = new ContinueToken("token-with-special_chars.123+/=");

            assertThat(token.value()).isEqualTo("token-with-special_chars.123+/=");
        }
    }
}
