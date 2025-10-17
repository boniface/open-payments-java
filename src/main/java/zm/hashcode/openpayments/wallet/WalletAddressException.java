package zm.hashcode.openpayments.wallet;

/**
 * Exception thrown when wallet address operations fail.
 *
 * <p>
 * This exception is thrown when:
 * <ul>
 * <li>A wallet address cannot be retrieved from the server</li>
 * <li>Public keys cannot be fetched</li>
 * <li>Response parsing fails</li>
 * <li>The server returns an error response</li>
 * </ul>
 */
public class WalletAddressException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new WalletAddressException with the specified message.
     *
     * @param message
     *            the error message
     */
    public WalletAddressException(String message) {
        super(message);
    }

    /**
     * Creates a new WalletAddressException with the specified message and cause.
     *
     * @param message
     *            the error message
     * @param cause
     *            the underlying cause
     */
    public WalletAddressException(String message, Throwable cause) {
        super(message, cause);
    }
}
