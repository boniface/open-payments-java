package zm.hashcode.openpayments.http.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import zm.hashcode.openpayments.http.config.HttpClientConfig;
import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.core.HttpMethod;
import zm.hashcode.openpayments.http.core.HttpRequest;
import zm.hashcode.openpayments.http.core.HttpResponse;
import zm.hashcode.openpayments.http.interceptor.RequestInterceptor;
import zm.hashcode.openpayments.http.interceptor.ResponseInterceptor;

/**
 * Apache HttpClient 5 implementation of {@link HttpClient}.
 *
 * <p>
 * This implementation uses Apache HttpClient 5's async client with:
 * <ul>
 * <li><b>Virtual Thread Support</b>: Leverages Java 21+ virtual threads for efficient async operations</li>
 * <li><b>Connection Pooling</b>: Efficient connection reuse across requests</li>
 * <li><b>HTTP/2 Support</b>: Automatic HTTP/2 upgrade where supported</li>
 * <li><b>Configurable Timeouts</b>: Connect, request, and socket timeouts</li>
 * <li><b>Interceptor Support</b>: Request and response interceptors for cross-cutting concerns</li>
 * </ul>
 *
 * <p>
 * The client automatically resolves relative URIs against the configured base URL, making it easy to work with REST
 * APIs.
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * var config = HttpClientConfig.builder().baseUrl("https://api.example.com").connectTimeout(Duration.ofSeconds(10))
 *         .build();
 *
 * var client = new ApacheHttpClient(config);
 *
 * var request = HttpRequest.builder().method(HttpMethod.GET).uri("/users/123") // Resolved to
 *                                                                              // https://api.example.com/users/123
 *         .build();
 *
 * var response = client.execute(request).join();
 * }</pre>
 *
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe and can be shared across multiple threads. The underlying Apache
 * HttpClient maintains an internal connection pool.
 */
public final class ApacheHttpClient implements HttpClient {

    private static final Logger LOGGER = Logger.getLogger(ApacheHttpClient.class.getName());

    private final HttpClientConfig config;
    private final CloseableHttpAsyncClient asyncClient;
    private final List<RequestInterceptor> requestInterceptors = new ArrayList<>();
    private final List<ResponseInterceptor> responseInterceptors = new ArrayList<>();

    /**
     * Creates a new Apache HTTP client with the specified configuration.
     *
     * @param config
     *            the HTTP client configuration
     */
    public ApacheHttpClient(HttpClientConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.asyncClient = buildAsyncClient(config);
        this.asyncClient.start();
        LOGGER.log(Level.INFO, "Apache HttpClient initialized with base URL: {0}", config.baseUrl());
    }

    private CloseableHttpAsyncClient buildAsyncClient(HttpClientConfig config) {
        // Configure connection pooling
        var connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(config.connectTimeout().toMillis(), TimeUnit.MILLISECONDS))
                .setSocketTimeout(Timeout.of(config.socketTimeout().toMillis(), TimeUnit.MILLISECONDS))
                .setTimeToLive(TimeValue.of(config.connectionTimeToLive().toMillis(), TimeUnit.MILLISECONDS)).build();

        var connectionManagerBuilder = PoolingAsyncClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig).setMaxConnTotal(config.maxConnections())
                .setMaxConnPerRoute(config.maxConnectionsPerRoute());

        // Add custom SSL context if provided
        config.getSslContext().ifPresent(sslContext -> {
            var tlsStrategy = ClientTlsStrategyBuilder.create().setSslContext(sslContext).buildAsync();
            connectionManagerBuilder.setTlsStrategy(tlsStrategy);
        });

        PoolingAsyncClientConnectionManager connectionManager = connectionManagerBuilder.build();

        // Configure request defaults
        var requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.of(config.requestTimeout().toMillis(), TimeUnit.MILLISECONDS))
                .setRedirectsEnabled(config.followRedirects()).build();

        // Configure IO reactor for virtual threads
        var ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(config.socketTimeout().toMillis())).build();

        // Build async client
        return HttpAsyncClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig)
                .setIOReactorConfig(ioReactorConfig)
                .setDefaultHeaders(List.of(new BasicHeader(HttpHeaders.USER_AGENT, "OpenPayments-Java-SDK/1.0")))
                .build();
    }

    @Override
    public CompletableFuture<HttpResponse> execute(HttpRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        // Apply request interceptors
        HttpRequest processedRequest = applyRequestInterceptors(request);

        // Resolve URI against base URL
        URI resolvedUri = resolveUri(processedRequest.uri());
        LOGGER.log(Level.FINE, "Executing {0} request to {1}", new Object[]{processedRequest.method(), resolvedUri});

        // Build Apache HttpClient request
        SimpleHttpRequest apacheRequest = buildApacheRequest(processedRequest, resolvedUri);

        // Execute request asynchronously using virtual threads
        var future = new CompletableFuture<HttpResponse>();

        asyncClient.execute(apacheRequest, new FutureCallback<>() {
            @Override
            public void completed(SimpleHttpResponse result) {
                try {
                    HttpResponse response = convertResponse(result);
                    HttpResponse processedResponse = applyResponseInterceptors(response);
                    future.complete(processedResponse);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void failed(Exception ex) {
                LOGGER.log(Level.WARNING, "Request failed: " + resolvedUri, ex);
                future.completeExceptionally(ex);
            }

            @Override
            public void cancelled() {
                LOGGER.log(Level.WARNING, "Request cancelled: {0}", resolvedUri);
                future.cancel(true);
            }
        });

        return future;
    }

    /**
     * Resolves a URI against the configured base URL.
     *
     * <p>
     * If the URI is absolute, it is returned as-is. If the URI is relative, it is resolved against the base URL.
     *
     * @param uri
     *            the URI to resolve
     * @return the resolved absolute URI
     */
    private URI resolveUri(URI uri) {
        if (uri.isAbsolute()) {
            return uri;
        }
        return config.baseUrl().resolve(uri);
    }

    private SimpleHttpRequest buildApacheRequest(HttpRequest request, URI uri) {
        Method apacheMethod = convertMethod(request.method());
        SimpleHttpRequest apacheRequest = SimpleHttpRequest.create(apacheMethod, uri);

        // Add headers
        request.headers().forEach(apacheRequest::addHeader);

        // Add body if present
        request.getBody().ifPresent(body -> {
            apacheRequest.setBody(body, ContentType.APPLICATION_JSON);
        });

        return apacheRequest;
    }

    private Method convertMethod(HttpMethod method) {
        return switch (method) {
            case GET -> Method.GET;
            case POST -> Method.POST;
            case PUT -> Method.PUT;
            case PATCH -> Method.PATCH;
            case DELETE -> Method.DELETE;
            case HEAD -> Method.HEAD;
            case OPTIONS -> Method.OPTIONS;
        };
    }

    private HttpResponse convertResponse(SimpleHttpResponse apacheResponse) throws IOException {
        int statusCode = apacheResponse.getCode();

        // Extract headers
        Map<String, String> headers = new HashMap<>();
        for (var header : apacheResponse.getHeaders()) {
            headers.put(header.getName(), header.getValue());
        }

        // Extract body
        String body = null;
        if (apacheResponse.getBodyBytes() != null) {
            body = new String(apacheResponse.getBodyBytes(), StandardCharsets.UTF_8);
        }

        return HttpResponse.of(statusCode, headers, body);
    }

    private HttpRequest applyRequestInterceptors(HttpRequest request) {
        HttpRequest current = request;
        for (RequestInterceptor interceptor : requestInterceptors) {
            current = interceptor.intercept(current);
        }
        return current;
    }

    private HttpResponse applyResponseInterceptors(HttpResponse response) {
        HttpResponse current = response;
        for (ResponseInterceptor interceptor : responseInterceptors) {
            current = interceptor.intercept(current);
        }
        return current;
    }

    @Override
    public void addRequestInterceptor(RequestInterceptor interceptor) {
        Objects.requireNonNull(interceptor, "interceptor must not be null");
        requestInterceptors.add(interceptor);
        LOGGER.log(Level.FINE, "Added request interceptor: {0}", interceptor.getClass().getName());
    }

    @Override
    public void addResponseInterceptor(ResponseInterceptor interceptor) {
        Objects.requireNonNull(interceptor, "interceptor must not be null");
        responseInterceptors.add(interceptor);
        LOGGER.log(Level.FINE, "Added response interceptor: {0}", interceptor.getClass().getName());
    }

    @Override
    public void close() {
        LOGGER.log(Level.INFO, "Closing Apache HttpClient");
        asyncClient.close(CloseMode.GRACEFUL);
    }
}
