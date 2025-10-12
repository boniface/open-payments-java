package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Access}.
 */
@DisplayName("Access")
class AccessTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create incoming payment access")
        void shouldCreateIncomingPayment() {
            Access access = Access.incomingPayment(List.of("create", "read"));

            assertThat(access.type()).isEqualTo("incoming-payment");
            assertThat(access.actions()).contains(List.of("create", "read"));
            assertThat(access.identifier()).isEmpty();
            assertThat(access.limits()).isEmpty();
        }

        @Test
        @DisplayName("should create outgoing payment access")
        void shouldCreateOutgoingPayment() {
            Limits limits = Limits.builder().debitAmount(new Amount("10000", "USD", 2)).build();

            Access access = Access.outgoingPayment("https://wallet.example/alice", List.of("create", "read"), limits);

            assertThat(access.type()).isEqualTo("outgoing-payment");
            assertThat(access.actions()).contains(List.of("create", "read"));
            assertThat(access.identifier()).contains("https://wallet.example/alice");
            assertThat(access.limits()).contains(limits);
        }

        @Test
        @DisplayName("should create quote access")
        void shouldCreateQuote() {
            Access access = Access.quote(List.of("create", "read"));

            assertThat(access.type()).isEqualTo("quote");
            assertThat(access.actions()).contains(List.of("create", "read"));
            assertThat(access.identifier()).isEmpty();
            assertThat(access.limits()).isEmpty();
        }

        @Test
        @DisplayName("should create custom access")
        void shouldCreateCustom() {
            Access access = Access.custom("custom-type", List.of("action1", "action2"));

            assertThat(access.type()).isEqualTo("custom-type");
            assertThat(access.actions()).contains(List.of("action1", "action2"));
        }
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should throw when type is null")
        void shouldThrowWhenTypeIsNull() {
            assertThatThrownBy(() -> new Access(null, null, null, null)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("type must not be null");
        }

        @Test
        @DisplayName("should handle null optional fields")
        void shouldHandleNullOptionalFields() {
            Access access = new Access("test-type", null, null, null);

            assertThat(access.type()).isEqualTo("test-type");
            assertThat(access.actions()).isEmpty();
            assertThat(access.identifier()).isEmpty();
            assertThat(access.limits()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Actions")
    class ActionsTests {

        @Test
        @DisplayName("should handle single action")
        void shouldHandleSingleAction() {
            Access access = Access.incomingPayment(List.of("read"));

            assertThat(access.actions()).contains(List.of("read"));
        }

        @Test
        @DisplayName("should handle multiple actions")
        void shouldHandleMultipleActions() {
            Access access = Access.incomingPayment(List.of("create", "read", "complete", "list"));

            assertThat(access.actions()).contains(List.of("create", "read", "complete", "list"));
        }

        @Test
        @DisplayName("should handle empty actions list")
        void shouldHandleEmptyActions() {
            Access access = new Access("test", java.util.Optional.of(List.of()), null, null);

            assertThat(access.actions()).contains(List.of());
        }
    }
}
