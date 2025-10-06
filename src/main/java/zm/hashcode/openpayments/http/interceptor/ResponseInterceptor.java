package zm.hashcode.openpayments.http.interceptor;

import zm.hashcode.openpayments.http.core.HttpResponse;

/**
 * Interceptor for HTTP responses.
 *
 * <p>
 * Response interceptors can inspect responses, log them, handle errors, etc.
 */
@FunctionalInterface
public interface ResponseInterceptor {

    /**
     * Intercepts and potentially processes an HTTP response after it is received.
     *
     * @param response
     *            the HTTP response
     * @return the response (potentially modified or wrapped)
     */
    HttpResponse intercept(HttpResponse response);
}
