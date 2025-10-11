package zm.hashcode.openpayments.auth.exception;

/**
 * Exception thrown when HTTP signature operations fail.
 *
 * <p>
 * This exception indicates failures in creating or validating HTTP message signatures, including cryptographic
 * failures, malformed signature headers, or missing signature components.
 *
 * <p>
 * Common scenarios:
 * <ul>
 * <li>Signature creation fails due to invalid private key</li>
 * <li>Signature validation fails due to mismatch</li>
 * <li>Required signature components are missing</li>
 * <li>Signature header format is invalid</li>
 * </ul>
 */
public class SignatureException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new signature exception with the specified detail message.
     *
     * @param message
     *            the detail message
     */
    public SignatureException(String message) {
        super(message);
    }

    /**
     * Constructs a new signature exception with the specified detail message and cause.
     *
     * @param message
     *            the detail message
     * @param cause
     *            the cause of this exception
     */
    public SignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
