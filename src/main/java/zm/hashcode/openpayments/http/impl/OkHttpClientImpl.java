package zm.hashcode.openpayments.http.impl;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.jetbrains.annotations.NotNull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zm.hashcode.openpayments.http.config.HttpClientConfig;
import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.core.HttpMethod;
import zm.hashcode.openpayments.http.core.HttpRequest;
import zm.hashcode.openpayments.http.core.HttpResponse;
import zm.hashcode.openpayments.http.interceptor.RequestInterceptor;
import zm.hashcode.openpayments.http.interceptor.ResponseInterceptor;

/**
 * OkHttp implementation of {@link HttpClient}.
 *
 * <p>
 * This implementation uses OkHttp 4's async client with:
 * <ul>
 * <li><b>High Performance</b>: Optimized for Android and Java applications</li>
 * <li><b>Connection Pooling</b>: Automatic connection reuse</li>
 * <li><b>HTTP/2 Support</b>: Automatic HTTP/2 upgrade</li>
 * <li><b>Configurable Timeouts</b>: Connect, read, and write timeouts</li>
 * <li><b>Interceptor Support</b>: Request and response interceptors</li>
 * </ul>
 *
 * <p>
 * OkHttp is particularly well-suited for:
 * <ul>
 * <li>Android applications (it's the default HTTP client in Android)</li>
 * <li>Applications requiring HTTP/2 or WebSocket support</li>
 * <li>Scenarios where you need fine-grained control over caching</li>
 * <li>Applications with strict memory constraints</li>
 * </ul>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * var config = HttpClientConfig.builder().baseUrl("https://api.example.com").connectTimeout(Duration.ofSeconds(10))
 *         .build();
 *
 * var client = new OkHttpClientImpl(config);
 *
 * var request = HttpRequest.builder().method(HttpMethod.GET).uri("/users/123").build();
 *
 * var response = client.execute(request).join();
 * }</pre>
 *
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe and can be shared across multiple threads. OkHttp maintains an
 * internal connection pool and dispatcher.
 */
public final class OkHttpClientImpl implements HttpClient {

    private static final Logger LOGGER = Logger.getLogger(OkHttpClientImpl.class.getName());
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final HttpClientConfig config;
    private final OkHttpClient okHttpClient;
    private final List<RequestInterceptor> requestInterceptors = new CopyOnWriteArrayList<>();
    private final List<ResponseInterceptor> responseInterceptors = new CopyOnWriteArrayList<>();

    /**
     * Creates a new OkHttp client with the specified configuration.
     *
     * @param config
     *            the HTTP client configuration
     */
    public OkHttpClientImpl(HttpClientConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.okHttpClient = buildOkHttpClient(config);
        LOGGER.log(Level.INFO, "OkHttp client initialized with base URL: {0}", config.baseUrl());
    }

    private OkHttpClient buildOkHttpClient(HttpClientConfig config) {
        var builder = new OkHttpClient.Builder()
                .connectTimeout(config.connectTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(config.socketTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.socketTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .callTimeout(config.requestTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .followRedirects(config.followRedirects()).followSslRedirects(config.followRedirects())
                .connectionPool(new ConnectionPool(config.maxConnections(), config.connectionTimeToLive().toMinutes(),
                        TimeUnit.MINUTES));

        // Configure SSL context if provided
        config.getSslContext().ifPresent(sslContext -> {
            try {
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                X509TrustManager trustManager = getTrustManager(sslContext);
                builder.sslSocketFactory(sslSocketFactory, trustManager);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to configure SSL context: " + e.getMessage(), e);
            }
        });

        return builder.build();
    }

    /**
     * Extracts the X509TrustManager from the SSLContext.
     *
     * <p>
     * This is required by OkHttp for SSL configuration.
     *
     * @param sslContext
     *            the SSL context
     * @return the trust manager
     * @throws IllegalStateException
     *             if no X509TrustManager is found
     */
    private X509TrustManager getTrustManager(SSLContext sslContext) {
        try {
            var trustManagerFactory = javax.net.ssl.TrustManagerFactory
                    .getInstance(javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((java.security.KeyStore) null);

            for (var trustManager : trustManagerFactory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager x509TrustManager) {
                    return x509TrustManager;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize TrustManagerFactory", e);
        }
        throw new IllegalStateException("No X509TrustManager found in provided SSLContext");
    }

    @Override
    public CompletableFuture<HttpResponse> execute(HttpRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        // Apply request interceptors
        HttpRequest processedRequest = applyRequestInterceptors(request);

        // Resolve URI against base URL
        URI resolvedUri = resolveUri(processedRequest.uri());
        LOGGER.log(Level.FINE, "Executing {0} request to {1}", new Object[]{processedRequest.method(), resolvedUri});

        // Build OkHttp request
        Request okRequest = buildOkHttpRequest(processedRequest, resolvedUri);

        // Execute request asynchronously
        var future = new CompletableFuture<HttpResponse>();

        okHttpClient.newCall(okRequest).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                // Extract status and headers before try-with-resources to ensure they're available in catch blocks
                int statusCode = response.code();
                Map<String, String> headers = new HashMap<>();
                response.headers().forEach(pair -> headers.put(pair.getFirst(), pair.getSecond()));

                try (response) {
                    HttpResponse httpResponse = convertResponse(response, statusCode, headers);
                    HttpResponse processedResponse = applyResponseInterceptors(httpResponse);
                    future.complete(processedResponse);
                } catch (IOException e) {
                    // Body read failed but we got an HTTP response
                    LOGGER.log(Level.WARNING, "Failed to read response body from {0}, status: {1}",
                            new Object[]{call.request().url(), statusCode});

                    // Create a response with empty body to preserve status code
                    HttpResponse errorResponse = HttpResponse.of(statusCode, headers, "");
                    future.complete(errorResponse);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Unexpected error processing response from " + call.request().url(), e);
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LOGGER.log(Level.WARNING, "Request failed: " + resolvedUri, e);
                future.completeExceptionally(e);
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

    private Request buildOkHttpRequest(HttpRequest request, URI uri) {
        var builder = new Request.Builder().url(uri.toString());

        // Add headers
        request.headers().forEach(builder::addHeader);

        // Add method and body
        RequestBody requestBody = createRequestBody(request);
        builder.method(request.method().name(), requestBody);

        return builder.build();
    }

    private RequestBody createRequestBody(HttpRequest request) {
        if (request.getBody().isEmpty()) {
            // For methods that don't require a body (GET, HEAD, OPTIONS)
            if (request.method() == HttpMethod.GET || request.method() == HttpMethod.HEAD
                    || request.method() == HttpMethod.OPTIONS) {
                return null;
            }
            // For methods that require a body but none is provided
            return RequestBody.create("", null);
        }

        String body = request.getBody().get();
        MediaType mediaType = determineMediaType(request);
        return RequestBody.create(body, mediaType);
    }

    private MediaType determineMediaType(HttpRequest request) {
        return request.getHeader("Content-Type").map(MediaType::parse).orElse(JSON_MEDIA_TYPE);
    }

    /**
     * Converts an OkHttp Response to our HttpResponse format.
     *
     * @param okResponse
     *            the OkHttp response
     * @param statusCode
     *            the HTTP status code (already extracted)
     * @param headers
     *            the HTTP headers (already extracted)
     * @return the converted HttpResponse
     * @throws IOException
     *             if reading the response body fails
     */
    private HttpResponse convertResponse(Response okResponse, int statusCode, Map<String, String> headers)
            throws IOException {
        // Extract body
        String body = "";
        ResponseBody responseBody = okResponse.body();
        if (responseBody != null) {
            body = responseBody.string();
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
        LOGGER.log(Level.INFO, "Closing OkHttp client");
        okHttpClient.dispatcher().executorService().shutdown();
        okHttpClient.connectionPool().evictAll();
    }
}
