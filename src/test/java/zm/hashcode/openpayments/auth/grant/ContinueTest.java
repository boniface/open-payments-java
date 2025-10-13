package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link Continue}.
 */
@DisplayName("Continue")
class ContinueTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should create with required fields")
        void shouldCreateWithRequiredFields() {
            ContinueToken token = new ContinueToken("token-value");

            Continue continueInfo = new Continue(token, "https://auth.example.com/continue", Optional.empty());

            assertThat(continueInfo.accessToken()).isEqualTo(token);
            assertThat(continueInfo.uri()).isEqualTo("https://auth.example.com/continue");
            assertThat(continueInfo.waitSeconds()).isEmpty();
        }

        @Test
        @DisplayName("should create with wait seconds")
        void shouldCreateWithWaitSeconds() {
            ContinueToken token = new ContinueToken("token-value");

            Continue continueInfo = new Continue(token, "https://auth.example.com/continue", Optional.of(30));

            assertThat(continueInfo.waitSeconds()).contains(30);
        }

        @Test
        @DisplayName("should throw when accessToken is null")
        void shouldThrowWhenAccessTokenIsNull() {
            assertThatThrownBy(() -> new Continue(null, "https://example.com", Optional.empty()))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("accessToken must not be null");
        }

        @Test
        @DisplayName("should throw when uri is null")
        void shouldThrowWhenUriIsNull() {
            assertThatThrownBy(() -> new Continue(new ContinueToken("token"), null, Optional.empty()))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("uri must not be null");
        }

        @Test
        @DisplayName("should handle null wait seconds")
        void shouldHandleNullWaitSeconds() {
            Continue continueInfo = new Continue(new ContinueToken("token"), "https://example.com", null);

            assertThat(continueInfo.waitSeconds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Token Convenience Method")
    class TokenMethodTests {

        @Test
        @DisplayName("should return token value")
        void shouldReturnTokenValue() {
            Continue continueInfo = new Continue(new ContinueToken("my-token-value"), "https://example.com",
                    Optional.empty());

            assertThat(continueInfo.token()).isEqualTo("my-token-value");
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
            Continue continueInfo = new Continue(new ContinueToken("token-123"),
                    "https://auth.example.com/continue/xyz", Optional.of(60));

            String json = objectMapper.writeValueAsString(continueInfo);

            assertThat(json).contains("\"access_token\"");
            assertThat(json).contains("\"value\"");
            assertThat(json).contains("token-123");
            assertThat(json).contains("\"uri\"");
            assertThat(json).contains("https://auth.example.com/continue/xyz");
            assertThat(json).contains("\"wait\"");
            assertThat(json).contains("60");
        }

        @Test
        @DisplayName("should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            String json = """
                    {
                        "access_token": {
                            "value": "continue-token-value"
                        },
                        "uri": "https://auth.example.com/continue",
                        "wait": 30
                    }
                    """;

            Continue continueInfo = objectMapper.readValue(json, Continue.class);

            assertThat(continueInfo.token()).isEqualTo("continue-token-value");
            assertThat(continueInfo.uri()).isEqualTo("https://auth.example.com/continue");
            assertThat(continueInfo.waitSeconds()).contains(30);
        }

        @Test
        @DisplayName("should deserialize without wait")
        void shouldDeserializeWithoutWait() throws Exception {
            String json = """
                    {
                        "access_token": {
                            "value": "token"
                        },
                        "uri": "https://example.com"
                    }
                    """;

            Continue continueInfo = objectMapper.readValue(json, Continue.class);

            assertThat(continueInfo.waitSeconds()).isEmpty();
        }

        @Test
        @DisplayName("should not include absent wait in JSON")
        void shouldNotIncludeAbsentWait() throws Exception {
            Continue continueInfo = new Continue(new ContinueToken("token"), "https://example.com", Optional.empty());

            String json = objectMapper.writeValueAsString(continueInfo);

            assertThat(json).doesNotContain("\"wait\"");
        }
    }

    @Nested
    @DisplayName("Wait Seconds Values")
    class WaitSecondsTests {

        @Test
        @DisplayName("should handle short wait")
        void shouldHandleShortWait() {
            Continue continueInfo = new Continue(new ContinueToken("token"), "https://example.com", Optional.of(5));

            assertThat(continueInfo.waitSeconds()).contains(5);
        }

        @Test
        @DisplayName("should handle long wait")
        void shouldHandleLongWait() {
            Continue continueInfo = new Continue(new ContinueToken("token"), "https://example.com", Optional.of(3600));

            assertThat(continueInfo.waitSeconds()).contains(3600);
        }

        @Test
        @DisplayName("should handle zero wait")
        void shouldHandleZeroWait() {
            Continue continueInfo = new Continue(new ContinueToken("token"), "https://example.com", Optional.of(0));

            assertThat(continueInfo.waitSeconds()).contains(0);
        }
    }
}
