package zm.hashcode.openpayments.auth.exception;

/**
 * Exception thrown when key management operations fail.
 *
 * <p>
 * This exception indicates failures in generating, loading, storing, or validating cryptographic keys used for
 * authentication in the Open Payments system.
 *
 * <p>
 * Common scenarios:
 * <ul>
 * <li>Key generation fails</li>
 * <li>Invalid key format (not Ed25519)</li>
 * <li>Key loading fails (corrupt or missing key file)</li>
 * <li>JWK parsing fails</li>
 * <li>Key validation fails (missing required fields)</li>
 * </ul>
 */
public class KeyException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new key exception with the specified detail message.
     *
     * @param message
     *            the detail message
     */
    public KeyException(String message) {
        super(message);
    }

    /**
     * Constructs a new key exception with the specified detail message and cause.
     *
     * @param message
     *            the detail message
     * @param cause
     *            the cause of this exception
     */
    public KeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
