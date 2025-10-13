package zm.hashcode.openpayments.auth.grant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link Amount}.
 */
@DisplayName("Amount")
class AmountTest {

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should create amount with all fields")
        void shouldCreateAmount() {
            Amount amount = new Amount("10000", "USD", 2);

            assertThat(amount.value()).isEqualTo("10000");
            assertThat(amount.assetCode()).isEqualTo("USD");
            assertThat(amount.assetScale()).isEqualTo(2);
        }

        @Test
        @DisplayName("should throw when value is null")
        void shouldThrowWhenValueIsNull() {
            assertThatThrownBy(() -> new Amount(null, "USD", 2)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("value must not be null");
        }

        @Test
        @DisplayName("should throw when assetCode is null")
        void shouldThrowWhenAssetCodeIsNull() {
            assertThatThrownBy(() -> new Amount("10000", null, 2)).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("assetCode must not be null");
        }

        @Test
        @DisplayName("should throw when assetScale is negative")
        void shouldThrowWhenAssetScaleIsNegative() {
            assertThatThrownBy(() -> new Amount("10000", "USD", -1)).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("assetScale must not be negative");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 8, 18})
        @DisplayName("should accept valid asset scales")
        void shouldAcceptValidAssetScales(int scale) {
            Amount amount = new Amount("10000", "USD", scale);

            assertThat(amount.assetScale()).isEqualTo(scale);
        }
    }

    @Nested
    @DisplayName("Asset Types")
    class AssetTypeTests {

        @Test
        @DisplayName("should handle USD amount")
        void shouldHandleUsd() {
            Amount amount = new Amount("10000", "USD", 2); // $100.00

            assertThat(amount.value()).isEqualTo("10000");
            assertThat(amount.assetCode()).isEqualTo("USD");
            assertThat(amount.assetScale()).isEqualTo(2);
        }

        @Test
        @DisplayName("should handle BTC amount")
        void shouldHandleBtc() {
            Amount amount = new Amount("100000", "BTC", 8); // 0.001 BTC

            assertThat(amount.value()).isEqualTo("100000");
            assertThat(amount.assetCode()).isEqualTo("BTC");
            assertThat(amount.assetScale()).isEqualTo(8);
        }

        @Test
        @DisplayName("should handle EUR amount")
        void shouldHandleEur() {
            Amount amount = new Amount("5000", "EUR", 2); // €50.00

            assertThat(amount.assetCode()).isEqualTo("EUR");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle zero amount")
        void shouldHandleZeroAmount() {
            Amount amount = new Amount("0", "USD", 2);

            assertThat(amount.value()).isEqualTo("0");
        }

        @Test
        @DisplayName("should handle large amount")
        void shouldHandleLargeAmount() {
            Amount amount = new Amount("999999999999999999", "USD", 2);

            assertThat(amount.value()).isEqualTo("999999999999999999");
        }

        @Test
        @DisplayName("should handle zero scale")
        void shouldHandleZeroScale() {
            Amount amount = new Amount("100", "JPY", 0); // Japanese Yen has no decimals

            assertThat(amount.assetScale()).isEqualTo(0);
        }
    }
}
