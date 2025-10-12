package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link GrantResponse}.
 */
@DisplayName("GrantResponse")
class GrantResponseTest {

    @Nested
    @DisplayName("State Checks")
    class StateCheckTests {

        @Test
        @DisplayName("should identify interaction required")
        void shouldIdentifyInteractionRequired() {
            InteractResponse interact = new InteractResponse("https://auth.example.com/interact", "finish-token");

            GrantResponse response = new GrantResponse(Optional.empty(), Optional.empty(), Optional.of(interact));

            assertThat(response.requiresInteraction()).isTrue();
            assertThat(response.isPending()).isFalse();
            assertThat(response.isApproved()).isFalse();
        }

        @Test
        @DisplayName("should identify pending grant")
        void shouldIdentifyPending() {
            Continue continueInfo = new Continue(new ContinueToken("continue-token"),
                    "https://auth.example.com/continue", Optional.empty());

            GrantResponse response = new GrantResponse(Optional.of(continueInfo), Optional.empty(), Optional.empty());

            assertThat(response.requiresInteraction()).isFalse();
            assertThat(response.isPending()).isTrue();
            assertThat(response.isApproved()).isFalse();
        }

        @Test
        @DisplayName("should identify approved grant")
        void shouldIdentifyApproved() {
            AccessTokenResponse token = new AccessTokenResponse("access-token-value", "https://auth.example.com/manage",
                    Optional.of(3600L), List.of(Access.incomingPayment(List.of("create"))));

            GrantResponse response = new GrantResponse(Optional.empty(), Optional.of(token), Optional.empty());

            assertThat(response.requiresInteraction()).isFalse();
            assertThat(response.isPending()).isFalse();
            assertThat(response.isApproved()).isTrue();
        }

        @Test
        @DisplayName("should handle pending with interaction")
        void shouldHandlePendingWithInteraction() {
            Continue continueInfo = new Continue(new ContinueToken("token"), "https://example.com/continue",
                    Optional.empty());

            InteractResponse interact = new InteractResponse("https://example.com/interact", "finish");

            GrantResponse response = new GrantResponse(Optional.of(continueInfo), Optional.empty(),
                    Optional.of(interact));

            assertThat(response.requiresInteraction()).isTrue();
            assertThat(response.isPending()).isTrue();
            assertThat(response.isApproved()).isFalse();
        }

        @Test
        @DisplayName("should handle empty response")
        void shouldHandleEmptyResponse() {
            GrantResponse response = new GrantResponse(Optional.empty(), Optional.empty(), Optional.empty());

            assertThat(response.requiresInteraction()).isFalse();
            assertThat(response.isPending()).isFalse();
            assertThat(response.isApproved()).isFalse();
        }
    }

    @Nested
    @DisplayName("JSON Deserialization")
    class JsonDeserializationTests {

        private final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jdk8.Jdk8Module());

        @Test
        @DisplayName("should deserialize pending response")
        void shouldDeserializePendingResponse() throws Exception {
            String json = """
                    {
                        "continue": {
                            "access_token": {
                                "value": "continue-token-123"
                            },
                            "uri": "https://auth.example.com/continue/xyz",
                            "wait": 30
                        },
                        "interact": {
                            "redirect": "https://auth.example.com/interact",
                            "finish": "finish-token"
                        }
                    }
                    """;

            GrantResponse response = objectMapper.readValue(json, GrantResponse.class);

            assertThat(response.requiresInteraction()).isTrue();
            assertThat(response.isPending()).isTrue();
            assertThat(response.continueInfo()).isPresent();
            assertThat(response.continueInfo().get().token()).isEqualTo("continue-token-123");
        }

        @Test
        @DisplayName("should deserialize approved response")
        void shouldDeserializeApprovedResponse() throws Exception {
            String json = """
                    {
                        "access_token": {
                            "value": "access-token-xyz",
                            "manage": "https://auth.example.com/token/manage",
                            "expires_in": 3600,
                            "access": [
                                {
                                    "type": "incoming-payment",
                                    "actions": ["create", "read"]
                                }
                            ]
                        }
                    }
                    """;

            GrantResponse response = objectMapper.readValue(json, GrantResponse.class);

            assertThat(response.isApproved()).isTrue();
            assertThat(response.accessToken()).isPresent();
            assertThat(response.accessToken().get().value()).isEqualTo("access-token-xyz");
        }
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should handle null optional parameters")
        void shouldHandleNullOptionalParameters() {
            GrantResponse response = new GrantResponse(null, null, null);

            assertThat(response.continueInfo()).isEmpty();
            assertThat(response.accessToken()).isEmpty();
            assertThat(response.interact()).isEmpty();
        }
    }
}
