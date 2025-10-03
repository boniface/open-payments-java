package zm.hashcode.openpayments.model;

/**
 * Base exception for all Open Payments SDK errors.
 *
 * <p>
 * This exception and its subclasses represent various error conditions that can occur when interacting with the Open
 * Payments API.
 */
public class OpenPaymentsException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message
     *            the error message
     */
    public OpenPaymentsException(String message) {
        this(message, null, 0, null);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message
     *            the error message
     * @param cause
     *            the cause
     */
    public OpenPaymentsException(String message, Throwable cause) {
        this(message, cause, 0, null);
    }

    /**
     * Constructs a new exception with detailed error information.
     *
     * @param message
     *            the error message
     * @param cause
     *            the cause
     * @param statusCode
     *            the HTTP status code
     * @param errorCode
     *            the error code from the API
     */
    public OpenPaymentsException(String message, Throwable cause, int statusCode, String errorCode) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    /**
     * Returns the HTTP status code associated with this error, or 0 if not applicable.
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the error code from the API, or null if not available.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}
