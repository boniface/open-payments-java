package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link GrantRequest}.
 */
@DisplayName("GrantRequest")
class GrantRequestTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should build with required fields")
        void shouldBuildWithRequiredFields() {
            AccessTokenRequest accessToken = AccessTokenRequest.builder()
                    .addAccess(Access.incomingPayment(List.of("create"))).build();

            Client client = Client.builder().build();

            GrantRequest request = GrantRequest.builder().accessToken(accessToken).client(client).build();

            assertThat(request.accessToken()).isEqualTo(accessToken);
            assertThat(request.client()).isEqualTo(client);
            assertThat(request.interact()).isEmpty();
        }

        @Test
        @DisplayName("should build with interact")
        void shouldBuildWithInteract() {
            AccessTokenRequest accessToken = AccessTokenRequest.builder()
                    .addAccess(Access.incomingPayment(List.of("create"))).build();

            Client client = Client.builder().build();
            Interact interact = Interact.redirect("https://example.com/callback", "nonce");

            GrantRequest request = GrantRequest.builder().accessToken(accessToken).client(client).interact(interact)
                    .build();

            assertThat(request.interact()).contains(interact);
        }

        @Test
        @DisplayName("should throw when accessToken is null")
        void shouldThrowWhenAccessTokenIsNull() {
            assertThatThrownBy(() -> GrantRequest.builder().client(Client.builder().build()).build())
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("accessToken must not be null");
        }

        @Test
        @DisplayName("should throw when client is null")
        void shouldThrowWhenClientIsNull() {
            assertThatThrownBy(
                    () -> GrantRequest.builder()
                            .accessToken(AccessTokenRequest.builder()
                                    .addAccess(Access.incomingPayment(List.of("create"))).build())
                            .build())
                    .isInstanceOf(NullPointerException.class).hasMessageContaining("client must not be null");
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
            GrantRequest request = GrantRequest.builder()
                    .accessToken(AccessTokenRequest.builder()
                            .addAccess(Access.incomingPayment(List.of("create", "read"))).build())
                    .client(Client.builder().key("https://example.com/jwks.json").display(Display.of("Test App"))
                            .build())
                    .build();

            String json = objectMapper.writeValueAsString(request);

            assertThat(json).contains("\"access_token\"");
            assertThat(json).contains("\"client\"");
            assertThat(json).contains("\"type\":\"incoming-payment\"");
        }

        @Test
        @DisplayName("should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            String json = """
                    {
                        "access_token": {
                            "access": [
                                {
                                    "type": "incoming-payment",
                                    "actions": ["create", "read"]
                                }
                            ]
                        },
                        "client": {
                            "key": "https://example.com/jwks.json"
                        }
                    }
                    """;

            GrantRequest request = objectMapper.readValue(json, GrantRequest.class);

            assertThat(request.accessToken()).isNotNull();
            assertThat(request.accessToken().access()).hasSize(1);
            assertThat(request.client()).isNotNull();
        }

        @Test
        @DisplayName("should not include absent optional fields")
        void shouldNotIncludeAbsentFields() throws Exception {
            GrantRequest request = GrantRequest.builder()
                    .accessToken(
                            AccessTokenRequest.builder().addAccess(Access.incomingPayment(List.of("create"))).build())
                    .client(Client.builder().build()).build();

            String json = objectMapper.writeValueAsString(request);

            assertThat(json).doesNotContain("\"interact\"");
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should support fluent builder")
        void shouldSupportFluentBuilder() {
            GrantRequest request = GrantRequest.builder()
                    .accessToken(
                            AccessTokenRequest.builder().addAccess(Access.incomingPayment(List.of("create"))).build())
                    .client(Client.builder().key("https://example.com/jwks.json").display(Display.of("My App")).build())
                    .interact(Interact.redirect("https://example.com/callback", "nonce")).build();

            assertThat(request).isNotNull();
            assertThat(request.accessToken()).isNotNull();
            assertThat(request.client()).isNotNull();
            assertThat(request.interact()).isPresent();
        }

        @Test
        @DisplayName("should handle null interact")
        void shouldHandleNullInteract() {
            GrantRequest request = GrantRequest.builder()
                    .accessToken(
                            AccessTokenRequest.builder().addAccess(Access.incomingPayment(List.of("create"))).build())
                    .client(Client.builder().build()).interact(null).build();

            assertThat(request.interact()).isEmpty();
        }
    }
}
