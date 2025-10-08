package zm.hashcode.openpayments.http.config;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import javax.net.ssl.SSLContext;

/**
 * Configuration for HTTP client instances.
 *
 * <p>
 * This immutable configuration class centralizes all HTTP client settings including timeouts, connection pooling, SSL
 * context, and the base URL for API endpoints.
 *
 * <p>
 * Use the builder pattern to construct instances:
 *
 * <pre>{@code
 * var config = HttpClientConfig.builder().baseUrl("https://api.example.com").connectTimeout(Duration.ofSeconds(10))
 *         .requestTimeout(Duration.ofSeconds(30)).build();
 * }</pre>
 *
 * @param baseUrl
 *            the base URL for all API requests
 * @param connectTimeout
 *            timeout for establishing connections
 * @param requestTimeout
 *            timeout for complete request/response cycle
 * @param socketTimeout
 *            timeout for socket read operations
 * @param maxConnections
 *            maximum total connections in the pool
 * @param maxConnectionsPerRoute
 *            maximum connections per route
 * @param connectionTimeToLive
 *            time-to-live for pooled connections
 * @param followRedirects
 *            whether to follow HTTP redirects automatically
 * @param sslContext
 *            custom SSL context (optional)
 */
public record HttpClientConfig(URI baseUrl, Duration connectTimeout, Duration requestTimeout, Duration socketTimeout,
        int maxConnections, int maxConnectionsPerRoute, Duration connectionTimeToLive, boolean followRedirects,
        SSLContext sslContext) {

    /**
     * Default connect timeout (10 seconds).
     */
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Default request timeout (30 seconds).
     */
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Default socket timeout (30 seconds).
     */
    public static final Duration DEFAULT_SOCKET_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Default maximum connections (100).
     */
    public static final int DEFAULT_MAX_CONNECTIONS = 100;

    /**
     * Default maximum connections per route (20).
     */
    public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 20;

    /**
     * Default connection time-to-live (5 minutes).
     */
    public static final Duration DEFAULT_CONNECTION_TTL = Duration.ofMinutes(5);

    /**
     * Default follow redirects setting (true).
     */
    public static final boolean DEFAULT_FOLLOW_REDIRECTS = true;

    public HttpClientConfig {
        Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        Objects.requireNonNull(connectTimeout, "connectTimeout must not be null");
        Objects.requireNonNull(requestTimeout, "requestTimeout must not be null");
        Objects.requireNonNull(socketTimeout, "socketTimeout must not be null");
        Objects.requireNonNull(connectionTimeToLive, "connectionTimeToLive must not be null");

        if (maxConnections <= 0) {
            throw new IllegalArgumentException("maxConnections must be positive");
        }
        if (maxConnectionsPerRoute <= 0) {
            throw new IllegalArgumentException("maxConnectionsPerRoute must be positive");
        }
        if (maxConnectionsPerRoute > maxConnections) {
            throw new IllegalArgumentException("maxConnectionsPerRoute cannot exceed maxConnections");
        }
    }

    /**
     * Returns the SSL context, if configured.
     *
     * @return an Optional containing the SSL context
     */
    public Optional<SSLContext> getSslContext() {
        return Optional.ofNullable(sslContext);
    }

    /**
     * Creates a new builder for constructing HttpClientConfig instances.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for constructing HttpClientConfig instances.
     */
    public static final class Builder {
        private URI baseUrl;
        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;
        private Duration socketTimeout = DEFAULT_SOCKET_TIMEOUT;
        private int maxConnections = DEFAULT_MAX_CONNECTIONS;
        private int maxConnectionsPerRoute = DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
        private Duration connectionTimeToLive = DEFAULT_CONNECTION_TTL;
        private boolean followRedirects = DEFAULT_FOLLOW_REDIRECTS;
        private SSLContext sslContext;

        private Builder() {
        }

        /**
         * Sets the base URL for all API requests.
         *
         * @param baseUrl
         *            the base URL
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = URI.create(baseUrl);
            return this;
        }

        /**
         * Sets the base URL for all API requests.
         *
         * @param baseUrl
         *            the base URL
         * @return this builder
         */
        public Builder baseUrl(URI baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets the timeout for establishing connections.
         *
         * @param connectTimeout
         *            the connect timeout
         * @return this builder
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Sets the timeout for complete request/response cycle.
         *
         * @param requestTimeout
         *            the request timeout
         * @return this builder
         */
        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        /**
         * Sets the timeout for socket read operations.
         *
         * @param socketTimeout
         *            the socket timeout
         * @return this builder
         */
        public Builder socketTimeout(Duration socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        /**
         * Sets the maximum total connections in the pool.
         *
         * @param maxConnections
         *            the maximum connections
         * @return this builder
         */
        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        /**
         * Sets the maximum connections per route.
         *
         * @param maxConnectionsPerRoute
         *            the maximum connections per route
         * @return this builder
         */
        public Builder maxConnectionsPerRoute(int maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
            return this;
        }

        /**
         * Sets the time-to-live for pooled connections.
         *
         * @param connectionTimeToLive
         *            the connection time-to-live
         * @return this builder
         */
        public Builder connectionTimeToLive(Duration connectionTimeToLive) {
            this.connectionTimeToLive = connectionTimeToLive;
            return this;
        }

        /**
         * Sets whether to follow HTTP redirects automatically.
         *
         * @param followRedirects
         *            true to follow redirects
         * @return this builder
         */
        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        /**
         * Sets a custom SSL context.
         *
         * @param sslContext
         *            the SSL context
         * @return this builder
         */
        public Builder sslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        /**
         * Builds the HttpClientConfig instance.
         *
         * @return a new HttpClientConfig instance
         * @throws IllegalArgumentException
         *             if required fields are missing or invalid
         */
        public HttpClientConfig build() {
            return new HttpClientConfig(baseUrl, connectTimeout, requestTimeout, socketTimeout, maxConnections,
                    maxConnectionsPerRoute, connectionTimeToLive, followRedirects, sslContext);
        }
    }
}
