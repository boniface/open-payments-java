package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link Limits}.
 */
@DisplayName("Limits")
class LimitsTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should build with debit amount")
        void shouldBuildWithDebitAmount() {
            Amount debitAmount = new Amount("10000", "USD", 2);

            Limits limits = Limits.builder().debitAmount(debitAmount).build();

            assertThat(limits.debitAmount()).contains(debitAmount);
            assertThat(limits.receiveAmount()).isEmpty();
            assertThat(limits.interval()).isEmpty();
        }

        @Test
        @DisplayName("should build with receive amount")
        void shouldBuildWithReceiveAmount() {
            Amount receiveAmount = new Amount("5000", "EUR", 2);

            Limits limits = Limits.builder().receiveAmount(receiveAmount).build();

            assertThat(limits.receiveAmount()).contains(receiveAmount);
            assertThat(limits.debitAmount()).isEmpty();
        }

        @Test
        @DisplayName("should build with interval")
        void shouldBuildWithInterval() {
            Limits limits = Limits.builder().interval("P1D").build();

            assertThat(limits.interval()).contains("P1D");
        }

        @Test
        @DisplayName("should build with all fields")
        void shouldBuildWithAllFields() {
            Amount debitAmount = new Amount("10000", "USD", 2);
            Amount receiveAmount = new Amount("5000", "EUR", 2);

            Limits limits = Limits.builder().debitAmount(debitAmount).receiveAmount(receiveAmount).interval("P1M")
                    .build();

            assertThat(limits.debitAmount()).contains(debitAmount);
            assertThat(limits.receiveAmount()).contains(receiveAmount);
            assertThat(limits.interval()).contains("P1M");
        }

        @Test
        @DisplayName("should build with no fields")
        void shouldBuildWithNoFields() {
            Limits limits = Limits.builder().build();

            assertThat(limits.debitAmount()).isEmpty();
            assertThat(limits.receiveAmount()).isEmpty();
            assertThat(limits.interval()).isEmpty();
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
            Limits limits = Limits.builder().debitAmount(new Amount("10000", "USD", 2)).interval("P1D").build();

            String json = objectMapper.writeValueAsString(limits);

            assertThat(json).contains("\"debitAmount\"");
            assertThat(json).contains("\"interval\"");
            assertThat(json).contains("\"P1D\"");
        }

        @Test
        @DisplayName("should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            String json = """
                    {
                        "debitAmount": {
                            "value": "10000",
                            "assetCode": "USD",
                            "assetScale": 2
                        },
                        "interval": "P1D"
                    }
                    """;

            Limits limits = objectMapper.readValue(json, Limits.class);

            assertThat(limits.debitAmount()).isPresent();
            assertThat(limits.debitAmount().get().value()).isEqualTo("10000");
            assertThat(limits.interval()).contains("P1D");
        }

        @Test
        @DisplayName("should not include absent fields")
        void shouldNotIncludeAbsentFields() throws Exception {
            Limits limits = Limits.builder().debitAmount(new Amount("10000", "USD", 2)).build();

            String json = objectMapper.writeValueAsString(limits);

            assertThat(json).contains("\"debitAmount\"");
            assertThat(json).doesNotContain("\"receiveAmount\"");
            assertThat(json).doesNotContain("\"interval\"");
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should support fluent builder")
        void shouldSupportFluentBuilder() {
            Limits limits = Limits.builder().debitAmount(new Amount("10000", "USD", 2))
                    .receiveAmount(new Amount("5000", "EUR", 2)).interval("P1W").build();

            assertThat(limits).isNotNull();
            assertThat(limits.debitAmount()).isPresent();
            assertThat(limits.receiveAmount()).isPresent();
            assertThat(limits.interval()).contains("P1W");
        }

        @Test
        @DisplayName("should handle null values")
        void shouldHandleNullValues() {
            Limits limits = Limits.builder().debitAmount(null).receiveAmount(null).interval(null).build();

            assertThat(limits.debitAmount()).isEmpty();
            assertThat(limits.receiveAmount()).isEmpty();
            assertThat(limits.interval()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Interval Formats")
    class IntervalFormatTests {

        @Test
        @DisplayName("should handle daily interval")
        void shouldHandleDailyInterval() {
            Limits limits = Limits.builder().interval("P1D").build();

            assertThat(limits.interval()).contains("P1D");
        }

        @Test
        @DisplayName("should handle weekly interval")
        void shouldHandleWeeklyInterval() {
            Limits limits = Limits.builder().interval("P1W").build();

            assertThat(limits.interval()).contains("P1W");
        }

        @Test
        @DisplayName("should handle monthly interval")
        void shouldHandleMonthlyInterval() {
            Limits limits = Limits.builder().interval("P1M").build();

            assertThat(limits.interval()).contains("P1M");
        }
    }
}
