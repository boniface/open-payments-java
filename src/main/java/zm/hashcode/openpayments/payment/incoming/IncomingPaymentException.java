package zm.hashcode.openpayments.payment.incoming;

/**
 * Exception thrown when incoming payment operations fail.
 *
 * <p>
 * This exception is thrown when:
 * <ul>
 * <li>An incoming payment cannot be created</li>
 * <li>An incoming payment cannot be retrieved from the server</li>
 * <li>Listing incoming payments fails</li>
 * <li>Completing an incoming payment fails</li>
 * <li>Response parsing fails</li>
 * <li>The server returns an error response</li>
 * </ul>
 */
public class IncomingPaymentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new IncomingPaymentException with the specified message.
     *
     * @param message
     *            the error message
     */
    public IncomingPaymentException(String message) {
        super(message);
    }

    /**
     * Creates a new IncomingPaymentException with the specified message and cause.
     *
     * @param message
     *            the error message
     * @param cause
     *            the underlying cause
     */
    public IncomingPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
