package zm.hashcode.openpayments.payment.outgoing;

/**
 * Exception thrown when an outgoing payment operation fails.
 *
 * <p>
 * This exception is thrown for various outgoing payment-related errors including:
 * <ul>
 * <li>HTTP request failures</li>
 * <li>Invalid response formats</li>
 * <li>JSON parsing errors</li>
 * <li>Validation failures</li>
 * </ul>
 */
public class OutgoingPaymentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new OutgoingPaymentException with the specified message.
     *
     * @param message
     *            the detail message
     */
    public OutgoingPaymentException(String message) {
        super(message);
    }

    /**
     * Creates a new OutgoingPaymentException with the specified message and cause.
     *
     * @param message
     *            the detail message
     * @param cause
     *            the cause of the exception
     */
    public OutgoingPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
