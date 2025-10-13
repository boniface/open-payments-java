package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link AccessTokenResponse}.
 */
@DisplayName("AccessTokenResponse")
class AccessTokenResponseTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should create with required fields")
        void shouldCreateWithRequiredFields() {
            AccessTokenResponse response = new AccessTokenResponse("access-token-value",
                    "https://auth.example.com/manage", Optional.empty(),
                    List.of(Access.incomingPayment(List.of("create"))));

            assertThat(response.value()).isEqualTo("access-token-value");
            assertThat(response.manage()).isEqualTo("https://auth.example.com/manage");
            assertThat(response.expiresIn()).isEmpty();
            assertThat(response.access()).hasSize(1);
        }

        @Test
        @DisplayName("should create with expires in")
        void shouldCreateWithExpiresIn() {
            AccessTokenResponse response = new AccessTokenResponse("token", "https://example.com/manage",
                    Optional.of(3600L), List.of());

            assertThat(response.expiresIn()).contains(3600L);
        }

        @Test
        @DisplayName("should throw when value is null")
        void shouldThrowWhenValueIsNull() {
            assertThatThrownBy(
                    () -> new AccessTokenResponse(null, "https://example.com/manage", Optional.empty(), List.of()))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("value must not be null");
        }

        @Test
        @DisplayName("should throw when manage is null")
        void shouldThrowWhenManageIsNull() {
            assertThatThrownBy(() -> new AccessTokenResponse("token", null, Optional.empty(), List.of()))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("manage must not be null");
        }

        @Test
        @DisplayName("should throw when access is null")
        void shouldThrowWhenAccessIsNull() {
            assertThatThrownBy(
                    () -> new AccessTokenResponse("token", "https://example.com/manage", Optional.empty(), null))
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("access must not be null");
        }

        @Test
        @DisplayName("should handle null expires in")
        void shouldHandleNullExpiresIn() {
            AccessTokenResponse response = new AccessTokenResponse("token", "https://example.com/manage", null,
                    List.of());

            assertThat(response.expiresIn()).isEmpty();
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
            AccessTokenResponse response = new AccessTokenResponse("access-token-xyz",
                    "https://auth.example.com/manage", Optional.of(3600L),
                    List.of(Access.incomingPayment(List.of("create", "read"))));

            String json = objectMapper.writeValueAsString(response);

            assertThat(json).contains("\"value\"");
            assertThat(json).contains("access-token-xyz");
            assertThat(json).contains("\"manage\"");
            assertThat(json).contains("https://auth.example.com/manage");
            assertThat(json).contains("\"expires_in\"");
            assertThat(json).contains("3600");
            assertThat(json).contains("\"access\"");
        }

        @Test
        @DisplayName("should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            String json = """
                    {
                        "value": "token-abc",
                        "manage": "https://auth.example.com/token/manage",
                        "expires_in": 7200,
                        "access": [
                            {
                                "type": "incoming-payment",
                                "actions": ["create", "read"]
                            },
                            {
                                "type": "quote",
                                "actions": ["create"]
                            }
                        ]
                    }
                    """;

            AccessTokenResponse response = objectMapper.readValue(json, AccessTokenResponse.class);

            assertThat(response.value()).isEqualTo("token-abc");
            assertThat(response.manage()).isEqualTo("https://auth.example.com/token/manage");
            assertThat(response.expiresIn()).contains(7200L);
            assertThat(response.access()).hasSize(2);
        }

        @Test
        @DisplayName("should deserialize without expires in")
        void shouldDeserializeWithoutExpiresIn() throws Exception {
            String json = """
                    {
                        "value": "token",
                        "manage": "https://example.com/manage",
                        "access": []
                    }
                    """;

            AccessTokenResponse response = objectMapper.readValue(json, AccessTokenResponse.class);

            assertThat(response.expiresIn()).isEmpty();
        }

        @Test
        @DisplayName("should not include absent expires in")
        void shouldNotIncludeAbsentExpiresIn() throws Exception {
            AccessTokenResponse response = new AccessTokenResponse("token", "https://example.com/manage",
                    Optional.empty(), List.of());

            String json = objectMapper.writeValueAsString(response);

            assertThat(json).doesNotContain("\"expires_in\"");
        }
    }

    @Nested
    @DisplayName("Access List")
    class AccessListTests {

        @Test
        @DisplayName("should support empty access list")
        void shouldSupportEmptyAccessList() {
            AccessTokenResponse response = new AccessTokenResponse("token", "https://example.com/manage",
                    Optional.empty(), List.of());

            assertThat(response.access()).isEmpty();
        }

        @Test
        @DisplayName("should support single access")
        void shouldSupportSingleAccess() {
            AccessTokenResponse response = new AccessTokenResponse("token", "https://example.com/manage",
                    Optional.empty(), List.of(Access.incomingPayment(List.of("create"))));

            assertThat(response.access()).hasSize(1);
        }

        @Test
        @DisplayName("should support multiple accesses")
        void shouldSupportMultipleAccesses() {
            AccessTokenResponse response = new AccessTokenResponse("token", "https://example.com/manage",
                    Optional.empty(),
                    List.of(Access.incomingPayment(List.of("create", "read")), Access.quote(List.of("create")),
                            Access.outgoingPayment("https://wallet.example/alice", List.of("create"),
                                    Limits.builder().build())));

            assertThat(response.access()).hasSize(3);
        }

        @Test
        @DisplayName("should create immutable access list")
        void shouldCreateImmutableAccessList() {
            AccessTokenResponse response = new AccessTokenResponse("token", "https://example.com/manage",
                    Optional.empty(), List.of(Access.incomingPayment(List.of("create"))));

            assertThatThrownBy(() -> response.access().add(Access.quote(List.of("read"))))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Expiration")
    class ExpirationTests {

        @Test
        @DisplayName("should handle short expiration")
        void shouldHandleShortExpiration() {
            AccessTokenResponse response = new AccessTokenResponse("token", "https://example.com/manage",
                    Optional.of(300L), List.of());

            assertThat(response.expiresIn()).contains(300L);
        }

        @Test
        @DisplayName("should handle long expiration")
        void shouldHandleLongExpiration() {
            AccessTokenResponse response = new AccessTokenResponse("token", "https://example.com/manage",
                    Optional.of(86400L), List.of());

            assertThat(response.expiresIn()).contains(86400L);
        }
    }
}
