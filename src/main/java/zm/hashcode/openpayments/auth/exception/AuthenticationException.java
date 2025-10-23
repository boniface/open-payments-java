package zm.hashcode.openpayments.auth.exception;

import zm.hashcode.openpayments.model.OpenPaymentsException;

/**
 * Base exception for authentication-related errors in the Open Payments system.
 *
 * <p>
 * This exception serves as the parent for all authentication-specific exceptions including signature failures, key
 * management errors, grant request failures, and token management issues.
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * try {
 *     var signature = signatureService.createSignature(request);
 * } catch (AuthenticationException e) {
 *     // Handle authentication error
 *     logger.error("Authentication failed", e);
 * }
 * }</pre>
 */
public class AuthenticationException extends OpenPaymentsException {

    /**
     * Constructs a new authentication exception with the specified detail message.
     *
     * @param message
     *            the detail message
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs a new authentication exception with the specified detail message and cause.
     *
     * @param message
     *            the detail message
     * @param cause
     *            the cause of this exception
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
