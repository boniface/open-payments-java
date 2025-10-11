package zm.hashcode.openpayments.http.factory;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import zm.hashcode.openpayments.http.config.HttpClientConfig;
import zm.hashcode.openpayments.http.config.HttpClientImplementation;
import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.impl.ApacheHttpClient;
import zm.hashcode.openpayments.http.impl.OkHttpClientImpl;
import zm.hashcode.openpayments.http.resilience.ResilienceConfig;
import zm.hashcode.openpayments.http.resilience.ResilientHttpClient;
import zm.hashcode.openpayments.http.resilience.RetryStrategy;

/**
 * Builder for constructing configured {@link HttpClient} instances.
 *
 * <p>
 * This builder provides a fluent API for creating HTTP clients with:
 * <ul>
 * <li><b>Base configuration</b>: Timeouts, connection pooling, SSL</li>
 * <li><b>Resilience</b>: Automatic retries, circuit breaker, backoff strategies</li>
 * <li><b>Interceptors</b>: Request/response interceptors for cross-cutting concerns</li>
 * </ul>
 *
 * <p>
 * The builder follows a decorator pattern, wrapping the base HTTP client implementation with resilience features when
 * configured.
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * var client = HttpClientBuilder.create().baseUrl("https://api.example.com").connectTimeout(Duration.ofSeconds(10))
 *         .requestTimeout(Duration.ofSeconds(30)).maxRetries(3)
 *         .retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100))).circuitBreakerEnabled(true)
 *         .addRequestInterceptor(loggingInterceptor).build();
 * }</pre>
 *
 * <p>
 * <b>Default Configuration:</b>
 * <ul>
 * <li>Connect timeout: 10 seconds</li>
 * <li>Request timeout: 30 seconds</li>
 * <li>Socket timeout: 30 seconds</li>
 * <li>Max connections: 100</li>
 * <li>Max connections per route: 20</li>
 * <li>Max retries: 3</li>
 * <li>Retry strategy: Exponential backoff with 100ms base delay</li>
 * <li>Circuit breaker: Enabled with 5 failure threshold</li>
 * </ul>
 */
public final class HttpClientBuilder {

    private final HttpClientConfig.Builder configBuilder = HttpClientConfig.builder();
    private final ResilienceConfig.Builder resilienceBuilder = ResilienceConfig.builder();

    private boolean resilienceEnabled = true;
    private HttpClientImplementation implementation = HttpClientImplementation.AUTO;

    private HttpClientBuilder() {
    }

    /**
     * Creates a new HTTP client builder.
     *
     * @return a new builder instance
     */
    public static HttpClientBuilder create() {
        return new HttpClientBuilder();
    }

    // ========================================
    // HTTP Client Configuration Methods
    // ========================================

    /**
     * Sets the base URL for all API requests.
     *
     * @param baseUrl
     *            the base URL
     * @return this builder
     */
    public HttpClientBuilder baseUrl(String baseUrl) {
        configBuilder.baseUrl(baseUrl);
        return this;
    }

    /**
     * Sets the base URL for all API requests.
     *
     * @param baseUrl
     *            the base URL
     * @return this builder
     */
    public HttpClientBuilder baseUrl(URI baseUrl) {
        configBuilder.baseUrl(baseUrl);
        return this;
    }

    /**
     * Sets the timeout for establishing connections.
     *
     * @param timeout
     *            the connect timeout
     * @return this builder
     */
    public HttpClientBuilder connectTimeout(Duration timeout) {
        configBuilder.connectTimeout(timeout);
        return this;
    }

    /**
     * Sets the timeout for complete request/response cycle.
     *
     * @param timeout
     *            the request timeout
     * @return this builder
     */
    public HttpClientBuilder requestTimeout(Duration timeout) {
        configBuilder.requestTimeout(timeout);
        return this;
    }

    /**
     * Sets the timeout for socket read operations.
     *
     * @param timeout
     *            the socket timeout
     * @return this builder
     */
    public HttpClientBuilder socketTimeout(Duration timeout) {
        configBuilder.socketTimeout(timeout);
        return this;
    }

    /**
     * Sets the maximum total connections in the pool.
     *
     * @param maxConnections
     *            the maximum connections
     * @return this builder
     */
    public HttpClientBuilder maxConnections(int maxConnections) {
        configBuilder.maxConnections(maxConnections);
        return this;
    }

    /**
     * Sets the maximum connections per route.
     *
     * @param maxConnectionsPerRoute
     *            the maximum connections per route
     * @return this builder
     */
    public HttpClientBuilder maxConnectionsPerRoute(int maxConnectionsPerRoute) {
        configBuilder.maxConnectionsPerRoute(maxConnectionsPerRoute);
        return this;
    }

    /**
     * Sets the time-to-live for pooled connections.
     *
     * @param ttl
     *            the connection time-to-live
     * @return this builder
     */
    public HttpClientBuilder connectionTimeToLive(Duration ttl) {
        configBuilder.connectionTimeToLive(ttl);
        return this;
    }

    /**
     * Sets whether to follow HTTP redirects automatically.
     *
     * @param followRedirects
     *            true to follow redirects
     * @return this builder
     */
    public HttpClientBuilder followRedirects(boolean followRedirects) {
        configBuilder.followRedirects(followRedirects);
        return this;
    }

    /**
     * Sets a custom SSL context.
     *
     * @param sslContext
     *            the SSL context
     * @return this builder
     */
    public HttpClientBuilder sslContext(SSLContext sslContext) {
        configBuilder.sslContext(sslContext);
        return this;
    }

    // ========================================
    // Resilience Configuration Methods
    // ========================================

    /**
     * Sets whether resilience features (retries, circuit breaker) are enabled.
     *
     * <p>
     * When disabled, the client will not perform retries or circuit breaking.
     *
     * @param enabled
     *            true to enable resilience features
     * @return this builder
     */
    public HttpClientBuilder resilienceEnabled(boolean enabled) {
        this.resilienceEnabled = enabled;
        return this;
    }

    /**
     * Sets the maximum number of retry attempts.
     *
     * @param maxRetries
     *            the maximum retries (0 to disable)
     * @return this builder
     */
    public HttpClientBuilder maxRetries(int maxRetries) {
        resilienceBuilder.maxRetries(maxRetries);
        return this;
    }

    /**
     * Sets the retry strategy for calculating delays.
     *
     * @param strategy
     *            the retry strategy
     * @return this builder
     */
    public HttpClientBuilder retryStrategy(RetryStrategy strategy) {
        resilienceBuilder.retryStrategy(strategy);
        return this;
    }

    /**
     * Sets the maximum delay between retries.
     *
     * @param maxDelay
     *            the maximum retry delay
     * @return this builder
     */
    public HttpClientBuilder maxRetryDelay(Duration maxDelay) {
        resilienceBuilder.maxRetryDelay(maxDelay);
        return this;
    }

    /**
     * Sets whether circuit breaker is enabled.
     *
     * @param enabled
     *            true to enable circuit breaker
     * @return this builder
     */
    public HttpClientBuilder circuitBreakerEnabled(boolean enabled) {
        resilienceBuilder.circuitBreakerEnabled(enabled);
        return this;
    }

    /**
     * Sets the circuit breaker failure threshold.
     *
     * @param threshold
     *            number of failures before opening circuit
     * @return this builder
     */
    public HttpClientBuilder circuitBreakerThreshold(int threshold) {
        resilienceBuilder.circuitBreakerThreshold(threshold);
        return this;
    }

    /**
     * Sets the circuit breaker timeout.
     *
     * @param timeout
     *            duration circuit stays open
     * @return this builder
     */
    public HttpClientBuilder circuitBreakerTimeout(Duration timeout) {
        resilienceBuilder.circuitBreakerTimeout(timeout);
        return this;
    }

    /**
     * Configures the HTTP client using a custom configuration.
     *
     * @param config
     *            the HTTP client configuration
     * @return this builder
     */
    public HttpClientBuilder withConfig(HttpClientConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        configBuilder.baseUrl(config.baseUrl()).connectTimeout(config.connectTimeout())
                .requestTimeout(config.requestTimeout()).socketTimeout(config.socketTimeout())
                .maxConnections(config.maxConnections()).maxConnectionsPerRoute(config.maxConnectionsPerRoute())
                .connectionTimeToLive(config.connectionTimeToLive()).followRedirects(config.followRedirects());

        config.getSslContext().ifPresent(configBuilder::sslContext);
        return this;
    }

    /**
     * Configures resilience using a custom configuration.
     *
     * @param config
     *            the resilience configuration
     * @return this builder
     */
    public HttpClientBuilder withResilience(ResilienceConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        resilienceBuilder.maxRetries(config.maxRetries()).retryStrategy(config.retryStrategy())
                .maxRetryDelay(config.maxRetryDelay()).retryOnStatusCodes(config.retryOnStatusCodes())
                .circuitBreakerEnabled(config.circuitBreakerEnabled())
                .circuitBreakerThreshold(config.circuitBreakerThreshold())
                .circuitBreakerTimeout(config.circuitBreakerTimeout())
                .circuitBreakerHalfOpenRequests(config.circuitBreakerHalfOpenRequests());
        return this;
    }

    /**
     * Sets the HTTP client implementation to use.
     *
     * <p>
     * This allows you to choose between different HTTP client libraries (Apache HttpClient, OkHttp, etc.).
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * // Use Apache HttpClient
     * HttpClient client = HttpClientBuilder.create().baseUrl("https://api.example.com")
     *         .implementation(HttpClientImplementation.APACHE).build();
     *
     * // Use OkHttp
     * HttpClient client = HttpClientBuilder.create().baseUrl("https://api.example.com")
     *         .implementation(HttpClientImplementation.OKHTTP).build();
     *
     * // Auto-detect (default)
     * HttpClient client = HttpClientBuilder.create().baseUrl("https://api.example.com")
     *         .implementation(HttpClientImplementation.AUTO).build();
     * }</pre>
     *
     * @param implementation
     *            the HTTP client implementation
     * @return this builder
     */
    public HttpClientBuilder implementation(HttpClientImplementation implementation) {
        this.implementation = Objects.requireNonNull(implementation, "implementation must not be null");
        return this;
    }

    /**
     * Builds and returns a configured HTTP client.
     *
     * <p>
     * The client is constructed as follows:
     * <ol>
     * <li>Determine which HTTP client implementation to use (Apache, OkHttp, or auto-detect)</li>
     * <li>Create base HTTP client with connection pooling and timeouts</li>
     * <li>Wrap with ResilientHttpClient if resilience is enabled</li>
     * </ol>
     *
     * @return a configured HTTP client instance
     * @throws IllegalArgumentException
     *             if required configuration is missing
     * @throws IllegalStateException
     *             if no HTTP client implementation is available
     */
    public HttpClient build() {
        HttpClientConfig config = configBuilder.build();

        // Determine implementation to use
        HttpClientImplementation implToUse = implementation == HttpClientImplementation.AUTO
                ? HttpClientImplementation.detectBestAvailable()
                : implementation;

        // Verify the selected implementation is available
        if (!implToUse.isAvailable()) {
            throw new IllegalStateException("HTTP client implementation " + implToUse.getDisplayName()
                    + " is not available on classpath. " + "Please add the required dependency.");
        }

        // Create base HTTP client based on implementation
        HttpClient baseClient = createBaseClient(config, implToUse);

        // Wrap with resilience decorator if enabled
        if (resilienceEnabled) {
            ResilienceConfig resilienceConfig = resilienceBuilder.build();
            return new ResilientHttpClient(baseClient, resilienceConfig);
        }

        return baseClient;
    }

    /**
     * Creates the base HTTP client for the specified implementation.
     *
     * @param config
     *            the HTTP client configuration
     * @param implementation
     *            the implementation to create
     * @return the base HTTP client
     */
    private HttpClient createBaseClient(HttpClientConfig config, HttpClientImplementation implementation) {
        return switch (implementation) {
            case APACHE -> new ApacheHttpClient(config);
            case OKHTTP -> new OkHttpClientImpl(config);
            case AUTO -> throw new IllegalStateException("AUTO should have been resolved to a concrete implementation");
        };
    }

    /**
     * Creates a simple HTTP client with minimal configuration.
     *
     * <p>
     * This is a convenience method for creating a client with:
     * <ul>
     * <li>Specified base URL</li>
     * <li>Default timeouts</li>
     * <li>Resilience enabled with defaults</li>
     * </ul>
     *
     * @param baseUrl
     *            the base URL for all requests
     * @return a configured HTTP client instance
     */
    public static HttpClient simple(String baseUrl) {
        return create().baseUrl(baseUrl).build();
    }

    /**
     * Creates an HTTP client with resilience disabled.
     *
     * <p>
     * This is useful for testing or scenarios where you want direct control over retries.
     *
     * @param baseUrl
     *            the base URL for all requests
     * @return a configured HTTP client instance without resilience
     */
    public static HttpClient withoutResilience(String baseUrl) {
        return create().baseUrl(baseUrl).resilienceEnabled(false).build();
    }
}
