package zm.hashcode.openpayments.http.interceptor;

import java.util.Map;
import java.util.Objects;

import zm.hashcode.openpayments.http.core.HttpRequest;

/**
 * Request interceptor that adds authentication headers to HTTP requests.
 *
 * <p>
 * This interceptor supports various authentication schemes:
 * <ul>
 * <li>Bearer tokens (OAuth 2.0, JWT)</li>
 * <li>GNAP tokens (Open Payments authentication)</li>
 * <li>Basic authentication</li>
 * <li>Custom header-based authentication</li>
 * </ul>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * // Bearer token authentication
 * AuthenticationInterceptor auth = AuthenticationInterceptor.bearer("my-access-token");
 * client.addRequestInterceptor(auth);
 *
 * // GNAP token authentication (Open Payments)
 * AuthenticationInterceptor gnap = AuthenticationInterceptor.gnap("my-gnap-token");
 * client.addRequestInterceptor(gnap);
 * }</pre>
 */
public record AuthenticationInterceptor(String authorizationHeaderValue) implements RequestInterceptor {

    /**
     * Creates an authentication interceptor with the given Authorization header value.
     *
     * @param authorizationHeaderValue
     *            the full value for the Authorization header
     */
    public AuthenticationInterceptor {
        Objects.requireNonNull(authorizationHeaderValue, "authorizationHeaderValue must not be null");
    }

    /**
     * Creates an interceptor for Bearer token authentication (OAuth 2.0, JWT).
     *
     * @param token
     *            the access token
     * @return a new authentication interceptor
     */
    public static AuthenticationInterceptor bearer(String token) {
        Objects.requireNonNull(token, "token must not be null");
        return new AuthenticationInterceptor("Bearer " + token);
    }

    /**
     * Creates an interceptor for GNAP token authentication (Open Payments).
     *
     * @param token
     *            the GNAP access token
     * @return a new authentication interceptor
     */
    public static AuthenticationInterceptor gnap(String token) {
        Objects.requireNonNull(token, "token must not be null");
        return new AuthenticationInterceptor("GNAP " + token);
    }

    /**
     * Creates an interceptor for Basic authentication.
     *
     * @param credentials
     *            the base64-encoded credentials (username:password)
     * @return a new authentication interceptor
     */
    public static AuthenticationInterceptor basic(String credentials) {
        Objects.requireNonNull(credentials, "credentials must not be null");
        return new AuthenticationInterceptor("Basic " + credentials);
    }

    /**
     * Creates an interceptor with a custom Authorization header value.
     *
     * @param scheme
     *            the authentication scheme (e.g., "Bearer", "GNAP", "Custom")
     * @param credentials
     *            the credentials
     * @return a new authentication interceptor
     */
    public static AuthenticationInterceptor custom(String scheme, String credentials) {
        Objects.requireNonNull(scheme, "scheme must not be null");
        Objects.requireNonNull(credentials, "credentials must not be null");
        return new AuthenticationInterceptor(scheme + " " + credentials);
    }

    @Override
    public HttpRequest intercept(HttpRequest request) {
        // Create new headers map with authentication
        Map<String, String> newHeaders = new java.util.concurrent.ConcurrentHashMap<>(request.headers());
        newHeaders.put("Authorization", authorizationHeaderValue);

        // Build new request with updated headers
        return HttpRequest.builder().method(request.method()).uri(request.uri()).headers(newHeaders)
                .body(request.getBody().orElse(null)).build();
    }

    /**
     * Returns the authorization header value (for testing purposes).
     *
     * @return the authorization header value
     */
    String getAuthorizationHeaderValue() {
        return authorizationHeaderValue;
    }
}
