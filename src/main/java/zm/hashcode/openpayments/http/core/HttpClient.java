package zm.hashcode.openpayments.http.core;

import java.util.concurrent.CompletableFuture;

import zm.hashcode.openpayments.http.interceptor.RequestInterceptor;
import zm.hashcode.openpayments.http.interceptor.ResponseInterceptor;

/**
 * HTTP client interface for Open Payments API communication.
 *
 * <p>
 * This abstraction allows for different HTTP client implementations and facilitates testing.
 */
public interface HttpClient extends AutoCloseable {

    /**
     * Executes an HTTP request asynchronously.
     *
     * @param request
     *            the HTTP request to execute
     * @return a CompletableFuture containing the HTTP response
     */
    CompletableFuture<HttpResponse> execute(HttpRequest request);

    /**
     * Adds a request interceptor.
     *
     * <p>
     * Interceptors are executed in the order they are added, before the request is sent.
     *
     * @param interceptor
     *            the interceptor to add
     */
    void addRequestInterceptor(RequestInterceptor interceptor);

    /**
     * Adds a response interceptor.
     *
     * <p>
     * Interceptors are executed in the order they are added, after the response is received.
     *
     * @param interceptor
     *            the interceptor to add
     */
    void addResponseInterceptor(ResponseInterceptor interceptor);

    /**
     * Closes the HTTP client and releases resources.
     */
    @Override
    void close();
}
