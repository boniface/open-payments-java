package zm.hashcode.openpayments.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class for input validation.
 *
 * <p>
 * This class provides common validation methods used throughout the SDK.
 */
public final class Validators {

    private Validators() {
        // Utility class
    }

    /**
     * Validates that a string is not null or blank.
     *
     * @param value
     *            the value to validate
     * @param fieldName
     *            the field name for error messages
     * @throws IllegalArgumentException
     *             if the value is null or blank
     */
    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
    }

    /**
     * Validates that an object is not null.
     *
     * @param value
     *            the value to validate
     * @param fieldName
     *            the field name for error messages
     * @param <T>
     *            the type of the value
     * @return the value if not null
     * @throws IllegalArgumentException
     *             if the value is null
     */
    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    /**
     * Validates that a string is a valid URI.
     *
     * @param value
     *            the string to validate
     * @param fieldName
     *            the field name for error messages
     * @return the parsed URI
     * @throws IllegalArgumentException
     *             if the string is not a valid URI
     */
    public static URI requireValidUri(String value, String fieldName) {
        requireNonBlank(value, fieldName);
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid URI", e);
        }
    }

    /**
     * Validates that a number is positive.
     *
     * @param value
     *            the value to validate
     * @param fieldName
     *            the field name for error messages
     * @throws IllegalArgumentException
     *             if the value is not positive
     */
    public static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    /**
     * Validates that a number is non-negative.
     *
     * @param value
     *            the value to validate
     * @param fieldName
     *            the field name for error messages
     * @throws IllegalArgumentException
     *             if the value is negative
     */
    public static void requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative");
        }
    }

    /**
     * Validates that a string matches a URL pattern (http/https).
     *
     * @param value
     *            the value to validate
     * @param fieldName
     *            the field name for error messages
     * @throws IllegalArgumentException
     *             if the value is not a valid URL
     */
    public static void requireValidUrl(String value, String fieldName) {
        var uri = requireValidUri(value, fieldName);
        var scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException(fieldName + " must be an HTTP or HTTPS URL");
        }
    }
}
