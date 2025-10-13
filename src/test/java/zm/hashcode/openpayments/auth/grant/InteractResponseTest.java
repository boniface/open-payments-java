package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link InteractResponse}.
 */
@DisplayName("InteractResponse")
class InteractResponseTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should create with redirect and finish")
        void shouldCreateWithRedirectAndFinish() {
            InteractResponse response = new InteractResponse("https://auth.example.com/interact", "finish-token-xyz");

            assertThat(response.redirect()).isEqualTo("https://auth.example.com/interact");
            assertThat(response.finish()).isEqualTo("finish-token-xyz");
        }

        @Test
        @DisplayName("should throw when redirect is null")
        void shouldThrowWhenRedirectIsNull() {
            assertThatThrownBy(() -> new InteractResponse(null, "finish-token"))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("redirect must not be null");
        }

        @Test
        @DisplayName("should throw when finish is null")
        void shouldThrowWhenFinishIsNull() {
            assertThatThrownBy(() -> new InteractResponse("https://example.com", null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("finish must not be null");
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
            InteractResponse response = new InteractResponse("https://auth.example.com/interact/abc",
                    "finish-token-123");

            String json = objectMapper.writeValueAsString(response);

            assertThat(json).contains("\"redirect\"");
            assertThat(json).contains("https://auth.example.com/interact/abc");
            assertThat(json).contains("\"finish\"");
            assertThat(json).contains("finish-token-123");
        }

        @Test
        @DisplayName("should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            String json = """
                    {
                        "redirect": "https://auth.example.com/interact",
                        "finish": "my-finish-token"
                    }
                    """;

            InteractResponse response = objectMapper.readValue(json, InteractResponse.class);

            assertThat(response.redirect()).isEqualTo("https://auth.example.com/interact");
            assertThat(response.finish()).isEqualTo("my-finish-token");
        }

        @Test
        @DisplayName("should round-trip through JSON")
        void shouldRoundTripThroughJson() throws Exception {
            InteractResponse original = new InteractResponse("https://test.example.com/interact", "finish-xyz");

            String json = objectMapper.writeValueAsString(original);
            InteractResponse deserialized = objectMapper.readValue(json, InteractResponse.class);

            assertThat(deserialized).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Equality")
    class EqualityTests {

        @Test
        @DisplayName("should be equal with same values")
        void shouldBeEqualWithSameValues() {
            InteractResponse response1 = new InteractResponse("https://example.com", "token");
            InteractResponse response2 = new InteractResponse("https://example.com", "token");

            assertThat(response1).isEqualTo(response2);
        }

        @Test
        @DisplayName("should not be equal with different redirect")
        void shouldNotBeEqualWithDifferentRedirect() {
            InteractResponse response1 = new InteractResponse("https://example1.com", "token");
            InteractResponse response2 = new InteractResponse("https://example2.com", "token");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("should not be equal with different finish")
        void shouldNotBeEqualWithDifferentFinish() {
            InteractResponse response1 = new InteractResponse("https://example.com", "token1");
            InteractResponse response2 = new InteractResponse("https://example.com", "token2");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("should have same hashCode with same values")
        void shouldHaveSameHashCodeWithSameValues() {
            InteractResponse response1 = new InteractResponse("https://example.com", "token");
            InteractResponse response2 = new InteractResponse("https://example.com", "token");

            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle complex redirect URL")
        void shouldHandleComplexRedirectUrl() {
            String complexUrl = "https://auth.example.com/interact?session=abc&state=xyz#fragment";

            InteractResponse response = new InteractResponse(complexUrl, "finish-token");

            assertThat(response.redirect()).isEqualTo(complexUrl);
        }

        @Test
        @DisplayName("should handle long finish token")
        void shouldHandleLongFinishToken() {
            String longToken = "f".repeat(1000);

            InteractResponse response = new InteractResponse("https://example.com", longToken);

            assertThat(response.finish()).hasSize(1000);
        }

        @Test
        @DisplayName("should handle empty finish token")
        void shouldHandleEmptyFinishToken() {
            InteractResponse response = new InteractResponse("https://example.com", "");

            assertThat(response.finish()).isEmpty();
        }
    }
}
