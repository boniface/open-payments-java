package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link AccessTokenRequest}.
 */
@DisplayName("AccessTokenRequest")
class AccessTokenRequestTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should build with single access")
        void shouldBuildWithSingleAccess() {
            AccessTokenRequest request = AccessTokenRequest.builder()
                    .addAccess(Access.incomingPayment(List.of("create"))).build();

            assertThat(request.access()).hasSize(1);
            assertThat(request.access().get(0).type()).isEqualTo("incoming-payment");
        }

        @Test
        @DisplayName("should build with multiple accesses")
        void shouldBuildWithMultipleAccesses() {
            AccessTokenRequest request = AccessTokenRequest.builder()
                    .addAccess(Access.incomingPayment(List.of("create", "read")))
                    .addAccess(Access.quote(List.of("create"))).build();

            assertThat(request.access()).hasSize(2);
            assertThat(request.access().get(0).type()).isEqualTo("incoming-payment");
            assertThat(request.access().get(1).type()).isEqualTo("quote");
        }

        @Test
        @DisplayName("should throw when access list is null")
        void shouldThrowWhenAccessListIsNull() {
            assertThatThrownBy(() -> new AccessTokenRequest(null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("access must not be null");
        }

        @Test
        @DisplayName("should throw when access list is empty")
        void shouldThrowWhenAccessListIsEmpty() {
            assertThatThrownBy(() -> new AccessTokenRequest(List.of())).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("access must not be empty");
        }

        @Test
        @DisplayName("should throw when building without access")
        void shouldThrowWhenBuildingWithoutAccess() {
            assertThatThrownBy(() -> AccessTokenRequest.builder().build()).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("access must not be empty");
        }

        @Test
        @DisplayName("should create immutable access list")
        void shouldCreateImmutableAccessList() {
            AccessTokenRequest request = AccessTokenRequest.builder()
                    .addAccess(Access.incomingPayment(List.of("create"))).build();

            assertThatThrownBy(() -> request.access().add(Access.quote(List.of("read"))))
                    .isInstanceOf(UnsupportedOperationException.class);
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
            AccessTokenRequest request = AccessTokenRequest.builder()
                    .addAccess(Access.incomingPayment(List.of("create", "read"))).build();

            String json = objectMapper.writeValueAsString(request);

            assertThat(json).contains("\"access\"");
            assertThat(json).contains("\"type\"");
            assertThat(json).contains("\"incoming-payment\"");
            assertThat(json).contains("\"actions\"");
        }

        @Test
        @DisplayName("should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            String json = """
                    {
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

            AccessTokenRequest request = objectMapper.readValue(json, AccessTokenRequest.class);

            assertThat(request.access()).hasSize(2);
            assertThat(request.access().get(0).type()).isEqualTo("incoming-payment");
            assertThat(request.access().get(1).type()).isEqualTo("quote");
        }

        @Test
        @DisplayName("should round-trip through JSON")
        void shouldRoundTripThroughJson() throws Exception {
            AccessTokenRequest original = AccessTokenRequest.builder()
                    .addAccess(Access.incomingPayment(List.of("create"))).addAccess(Access.quote(List.of("read")))
                    .build();

            String json = objectMapper.writeValueAsString(original);
            AccessTokenRequest deserialized = objectMapper.readValue(json, AccessTokenRequest.class);

            assertThat(deserialized.access()).hasSize(original.access().size());
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should support fluent builder")
        void shouldSupportFluentBuilder() {
            AccessTokenRequest request = AccessTokenRequest.builder()
                    .addAccess(Access.incomingPayment(List.of("create")))
                    .addAccess(Access.outgoingPayment("https://wallet.example/alice", List.of("create"),
                            Limits.builder().debitAmount(new Amount("10000", "USD", 2)).build()))
                    .build();

            assertThat(request).isNotNull();
            assertThat(request.access()).hasSize(2);
        }

        @Test
        @DisplayName("should accumulate multiple addAccess calls")
        void shouldAccumulateMultipleAddAccessCalls() {
            AccessTokenRequest.Builder builder = AccessTokenRequest.builder();

            builder.addAccess(Access.incomingPayment(List.of("create")));
            builder.addAccess(Access.quote(List.of("read")));
            builder.addAccess(Access.outgoingPayment("https://wallet.example/alice", List.of("create"),
                    Limits.builder().build()));

            AccessTokenRequest request = builder.build();

            assertThat(request.access()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Access Types")
    class AccessTypesTests {

        @Test
        @DisplayName("should support incoming payment access")
        void shouldSupportIncomingPaymentAccess() {
            AccessTokenRequest request = AccessTokenRequest.builder()
                    .addAccess(Access.incomingPayment(List.of("create", "read", "complete", "list"))).build();

            Access access = request.access().get(0);
            assertThat(access.type()).isEqualTo("incoming-payment");
            assertThat(access.actions()).contains(List.of("create", "read", "complete", "list"));
        }

        @Test
        @DisplayName("should support outgoing payment access with limits")
        void shouldSupportOutgoingPaymentAccess() {
            Limits limits = Limits.builder().debitAmount(new Amount("10000", "USD", 2)).interval("P1D").build();

            AccessTokenRequest request = AccessTokenRequest.builder()
                    .addAccess(
                            Access.outgoingPayment("https://wallet.example/alice", List.of("create", "read"), limits))
                    .build();

            Access access = request.access().get(0);
            assertThat(access.type()).isEqualTo("outgoing-payment");
            assertThat(access.identifier()).contains("https://wallet.example/alice");
            assertThat(access.limits()).isPresent();
        }

        @Test
        @DisplayName("should support quote access")
        void shouldSupportQuoteAccess() {
            AccessTokenRequest request = AccessTokenRequest.builder().addAccess(Access.quote(List.of("create", "read")))
                    .build();

            Access access = request.access().get(0);
            assertThat(access.type()).isEqualTo("quote");
        }
    }
}
