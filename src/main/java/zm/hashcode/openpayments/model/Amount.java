package zm.hashcode.openpayments.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a monetary amount in the Open Payments system.
 *
 * <p>
 * Amounts consist of a value and asset information (code and scale). The scale determines the precision of the amount.
 *
 * <p>
 * Example: USD $10.50 would be represented as value=1050, assetCode="USD", assetScale=2
 *
 * @param value
 *            the amount value as string
 * @param assetCode
 *            the ISO 4217 currency code
 * @param assetScale
 *            the decimal scale
 */
public record Amount(String value, String assetCode, int assetScale) {

    public Amount {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(assetCode, "assetCode must not be null");
    }

    /**
     * Creates a new Amount from string value.
     *
     * @param value
     *            the amount value as string
     * @param assetCode
     *            the ISO 4217 currency code
     * @param assetScale
     *            the decimal scale
     * @return a new Amount instance
     */
    public static Amount of(String value, String assetCode, int assetScale) {
        return new Amount(value, assetCode, assetScale);
    }

    /**
     * Creates a new Amount from BigDecimal value.
     *
     * @param value
     *            the amount value
     * @param assetCode
     *            the ISO 4217 currency code
     * @param assetScale
     *            the decimal scale
     * @return a new Amount instance
     */
    public static Amount of(BigDecimal value, String assetCode, int assetScale) {
        return new Amount(value.toPlainString(), assetCode, assetScale);
    }

    /**
     * Converts this amount to a BigDecimal.
     *
     * @return the amount as BigDecimal
     */
    public BigDecimal toBigDecimal() {
        return new BigDecimal(value).movePointLeft(assetScale);
    }
}
