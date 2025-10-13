package zm.hashcode.openpayments.auth.token;

import zm.hashcode.openpayments.auth.exception.AuthenticationException;

/**
 * Exception thrown when token management operations fail.
 *
 * <p>
 * This exception is thrown when token rotation, revocation, or other token management operations fail due to network
 * errors, invalid tokens, or authorization server errors.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9635#section-6">RFC 9635 Section 6 - Token Management</a>
 */
public class TokenException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new token exception with the specified message.
     *
     * @param message
     *            the detail message
     */
    public TokenException(String message) {
        super(message);
    }

    /**
     * Constructs a new token exception with the specified message and cause.
     *
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
