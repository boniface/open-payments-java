package zm.hashcode.openpayments.payment.quote;

/**
 * Exception thrown when a quote operation fails.
 *
 * <p>
 * This exception is thrown for various quote-related errors including:
 * <ul>
 * <li>HTTP request failures</li>
 * <li>Invalid response formats</li>
 * <li>JSON parsing errors</li>
 * <li>Validation failures</li>
 * </ul>
 */
public class QuoteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new QuoteException with the specified message.
     *
     * @param message
     *            the detail message
     */
    public QuoteException(String message) {
        super(message);
    }

    /**
     * Creates a new QuoteException with the specified message and cause.
     *
     * @param message
     *            the detail message
     * @param cause
     *            the cause of the exception
     */
    public QuoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
