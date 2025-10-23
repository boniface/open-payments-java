package zm.hashcode.openpayments.auth.grant;

import zm.hashcode.openpayments.auth.exception.AuthenticationException;

/**
 * Exception thrown when grant operations fail.
 *
 * <p>
 * This exception is thrown when grant requests, continuations, or cancellations fail due to network errors, invalid
 * responses, or authorization server errors.
 *
 * @see <a href="https://openpayments.dev/grants/">Open Payments - Grants</a>
 */
public class GrantException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new grant exception with the specified message.
     *
     * @param message
     *            the detail message
     */
    public GrantException(String message) {
        super(message);
    }

    /**
     * Constructs a new grant exception with the specified message and cause.
     *
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public GrantException(String message, Throwable cause) {
        super(message, cause);
    }
}
