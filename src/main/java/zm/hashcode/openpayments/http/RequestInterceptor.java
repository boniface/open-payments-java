package zm.hashcode.openpayments.http;

/**
 * Interceptor for HTTP requests.
 *
 * <p>
 * Request interceptors can modify requests before they are sent, add headers, log requests, etc.
 */
@FunctionalInterface
public interface RequestInterceptor {

    /**
     * Intercepts and potentially modifies an HTTP request before it is sent.
     *
     * @param request
     *            the original request
     * @return the modified request (or the original if no modifications needed)
     */
    HttpRequest intercept(HttpRequest request);
}
