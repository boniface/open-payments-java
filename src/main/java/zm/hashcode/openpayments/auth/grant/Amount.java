package zm.hashcode.openpayments.auth.grant;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Monetary amount with asset information.
 *
 * <p>
 * Represents a monetary value in a specific asset (currency) with a scale factor. The value is stored as a string to
 * preserve precision for financial calculations.
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * // $100.00 USD
 * Amount amount = new Amount("10000", "USD", 2);
 *
 * // 0.00001 BTC
 * Amount btc = new Amount("100000", "BTC", 8);
 * }</pre>
 *
 * @see <a href="https://openpayments.dev/grants/request/">Open Payments - Grant Request</a>
 */
public record Amount(@JsonProperty("value") String value, @JsonProperty("assetCode") String assetCode,
        @JsonProperty("assetScale") int assetScale) {

    public Amount {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(assetCode, "assetCode must not be null");
        if (assetScale < 0) {
            throw new IllegalArgumentException("assetScale must not be negative");
        }
    }
}
