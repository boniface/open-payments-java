package zm.hashcode.openpayments.http.factory;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import zm.hashcode.openpayments.http.config.HttpClientImplementation;
import zm.hashcode.openpayments.http.core.HttpClient;
import zm.hashcode.openpayments.http.resilience.RetryStrategy;

/**
 * Advanced factory for creating and managing HTTP client CLIENT_CACHE.
 *
 * <p>
 * This factory provides advanced capabilities beyond the basic {@link HttpClientBuilder}:
 * <ul>
 * <li><b>Named instances</b>: Create and retrieve clients by name</li>
 * <li><b>Instance caching</b>: Reuse client instances across application</li>
 * <li><b>Environment-specific configuration</b>: Different configs for dev/staging/prod</li>
 * <li><b>Service-specific clients</b>: Different implementations for different services</li>
 * </ul>
 *
 * <h2>Usage Patterns</h2>
 *
 * <h3>1. Single Global Instance</h3>
 *
 * <pre>{@code
 * // Create once at application startup
 * HttpClient client = HttpClientFactory.createDefault("https://api.example.com");
 *
 * // Reuse throughout application
 * HttpClient sameClient = HttpClientFactory.getDefault();
 * }</pre>
 *
 * <h3>2. Multiple Named Instances</h3>
 *
 * <pre>{@code
 * // Create different clients for different services
 * HttpClientFactory.register("payment-service", HttpClientBuilder.create().baseUrl("https://payments.example.com")
 *         .implementation(HttpClientImplementation.APACHE).maxRetries(5).build());
 *
 * HttpClientFactory.register("user-service", HttpClientBuilder.create().baseUrl("https://users.example.com")
 *         .implementation(HttpClientImplementation.OKHTTP).maxRetries(3).build());
 *
 * // Retrieve by name
 * HttpClient paymentClient = HttpClientFactory.get("payment-service");
 * HttpClient userClient = HttpClientFactory.get("user-service");
 * }</pre>
 *
 * <h3>3. Environment-Specific Configuration</h3>
 *
 * <pre>{@code
 * // Configure based on environment
 * Environment env = Environment.fromSystemProperty();
 * HttpClient client = HttpClientFactory.forEnvironment(env);
 * }</pre>
 *
 * <h3>4. Service-Specific Optimizations</h3>
 *
 * <pre>{@code
 * // Fast internal service (minimal retry, OkHttp)
 * HttpClient internal = HttpClientFactory.forInternalService("internal-api.example.com");
 *
 * // External unreliable API (aggressive retry, Apache)
 * HttpClient external = HttpClientFactory.forExternalService("external-api.example.com");
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * This factory is thread-safe and can be used concurrently across multiple threads.
 *
 * @see HttpClientBuilder
 * @see HttpClientImplementation
 */
public final class HttpClientFactory {

    private static final Map<String, HttpClient> CLIENT_CACHE = new ConcurrentHashMap<>();
    private static final String DEFAULT_NAME = "__default__";

    private HttpClientFactory() {
        // Prevent instantiation
    }

    /**
     * Creates and registers a default HTTP client instance.
     *
     * <p>
     * This is a convenience method for applications that only need a single HTTP client.
     *
     * @param baseUrl
     *            the base URL for all requests
     * @return the created HTTP client
     * @throws IllegalStateException
     *             if a default instance already exists
     */
    public static HttpClient createDefault(String baseUrl) {
        return createDefault(HttpClientBuilder.create().baseUrl(baseUrl).build());
    }

    /**
     * Registers a pre-configured HTTP client as the default instance.
     *
     * @param client
     *            the HTTP client to register
     * @return the registered HTTP client
     * @throws IllegalStateException
     *             if a default instance already exists
     */
    public static HttpClient createDefault(HttpClient client) {
        Objects.requireNonNull(client, "client must not be null");
        HttpClient existing = CLIENT_CACHE.putIfAbsent(DEFAULT_NAME, client);
        if (existing != null) {
            throw new IllegalStateException(
                    "Default HTTP client already exists. Use getDefault() or clearDefault() first.");
        }
        return client;
    }

    /**
     * Retrieves the default HTTP client instance.
     *
     * @return the default HTTP client
     * @throws IllegalStateException
     *             if no default instance has been created
     */
    public static HttpClient getDefault() {
        HttpClient client = CLIENT_CACHE.get(DEFAULT_NAME);
        if (client == null) {
            throw new IllegalStateException("No default HTTP client found. Call createDefault() first.");
        }
        return client;
    }

    /**
     * Clears the default HTTP client instance.
     *
     * <p>
     * This closes the client and removes it from the registry.
     */
    public static void clearDefault() {
        HttpClient client = CLIENT_CACHE.remove(DEFAULT_NAME);
        if (client != null) {
            client.close();
        }
    }

    /**
     * Registers a named HTTP client instance.
     *
     * @param name
     *            the name to register under
     * @param client
     *            the HTTP client to register
     * @return the registered HTTP client
     * @throws IllegalStateException
     *             if a client with this name already exists
     */
    public static HttpClient register(String name, HttpClient client) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(client, "client must not be null");

        HttpClient existing = CLIENT_CACHE.putIfAbsent(name, client);
        if (existing != null) {
            throw new IllegalStateException(
                    "HTTP client '" + name + "' already registered. Use get() or unregister() first.");
        }
        return client;
    }

    /**
     * Retrieves a named HTTP client instance.
     *
     * @param name
     *            the name to look up
     * @return the HTTP client
     * @throws IllegalStateException
     *             if no client with this name exists
     */
    public static HttpClient get(String name) {
        Objects.requireNonNull(name, "name must not be null");
        HttpClient client = CLIENT_CACHE.get(name);
        if (client == null) {
            throw new IllegalStateException("No HTTP client found with name: " + name);
        }
        return client;
    }

    /**
     * Checks if a named client exists.
     *
     * @param name
     *            the name to check
     * @return true if a client with this name exists
     */
    public static boolean exists(String name) {
        return CLIENT_CACHE.containsKey(name);
    }

    /**
     * Unregisters and closes a named HTTP client.
     *
     * @param name
     *            the name to unregister
     */
    public static void unregister(String name) {
        HttpClient client = CLIENT_CACHE.remove(name);
        if (client != null) {
            client.close();
        }
    }

    /**
     * Closes and unregisters all HTTP client CLIENT_CACHE.
     *
     * <p>
     * This should be called at application shutdown to release resources.
     */
    public static void closeAll() {
        CLIENT_CACHE.values().forEach(HttpClient::close);
        CLIENT_CACHE.clear();
    }

    // ========================================
    // Factory Methods for Common Scenarios
    // ========================================

    /**
     * Creates an HTTP client optimized for internal service communication.
     *
     * <p>
     * Characteristics:
     * <ul>
     * <li>OkHttp implementation (lightweight, fast)</li>
     * <li>Short timeouts (fast failure)</li>
     * <li>Minimal retries (internal services should be reliable)</li>
     * <li>Circuit breaker enabled</li>
     * </ul>
     *
     * @param baseUrl
     *            the base URL of the internal service
     * @return an optimized HTTP client for internal services
     */
    public static HttpClient forInternalService(String baseUrl) {
        return HttpClientBuilder.create().baseUrl(baseUrl).implementation(HttpClientImplementation.OKHTTP)
                .connectTimeout(Duration.ofSeconds(2)).requestTimeout(Duration.ofSeconds(5)).maxRetries(1)
                .retryStrategy(RetryStrategy.fixedDelay(Duration.ofMillis(100))).circuitBreakerEnabled(true)
                .circuitBreakerThreshold(3).circuitBreakerTimeout(Duration.ofSeconds(30)).build();
    }

    /**
     * Creates an HTTP client optimized for external service communication.
     *
     * <p>
     * Characteristics:
     * <ul>
     * <li>Apache HttpClient implementation (robust, feature-rich)</li>
     * <li>Longer timeouts (external services may be slower)</li>
     * <li>Aggressive retries with exponential backoff</li>
     * <li>Circuit breaker with longer timeout</li>
     * </ul>
     *
     * @param baseUrl
     *            the base URL of the external service
     * @return an optimized HTTP client for external services
     */
    public static HttpClient forExternalService(String baseUrl) {
        return HttpClientBuilder.create().baseUrl(baseUrl).implementation(HttpClientImplementation.APACHE)
                .connectTimeout(Duration.ofSeconds(10)).requestTimeout(Duration.ofSeconds(30)).maxRetries(5)
                .retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100)).withFullJitter())
                .maxRetryDelay(Duration.ofSeconds(30)).circuitBreakerEnabled(true).circuitBreakerThreshold(10)
                .circuitBreakerTimeout(Duration.ofMinutes(2)).build();
    }

    /**
     * Creates an HTTP client configured for the specified environment.
     *
     * @param environment
     *            the environment (DEV, STAGING, PRODUCTION)
     * @param baseUrl
     *            the base URL for the environment
     * @return an environment-specific HTTP client
     */
    public static HttpClient forEnvironment(Environment environment, String baseUrl) {
        return switch (environment) {
            case DEVELOPMENT -> HttpClientBuilder.create().baseUrl(baseUrl).connectTimeout(Duration.ofSeconds(5))
                    .requestTimeout(Duration.ofSeconds(10)).resilienceEnabled(false) // Fast feedback in dev
                    .build();

            case STAGING -> HttpClientBuilder.create().baseUrl(baseUrl).connectTimeout(Duration.ofSeconds(10))
                    .requestTimeout(Duration.ofSeconds(30)).maxRetries(3)
                    .retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100))).circuitBreakerEnabled(true)
                    .build();

            case PRODUCTION -> HttpClientBuilder.create().baseUrl(baseUrl)
                    .implementation(HttpClientImplementation.APACHE).connectTimeout(Duration.ofSeconds(10))
                    .requestTimeout(Duration.ofSeconds(30)).maxConnections(200).maxConnectionsPerRoute(50).maxRetries(5)
                    .retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100)).withFullJitter())
                    .circuitBreakerEnabled(true).circuitBreakerThreshold(10).build();
        };
    }

    /**
     * Application environment enumeration.
     */
    public enum Environment {
        DEVELOPMENT, STAGING, PRODUCTION;

        /**
         * Determines environment from system property "app.environment".
         *
         * @return the current environment, defaults to DEVELOPMENT
         */
        public static Environment fromSystemProperty() {
            String env = System.getProperty("app.environment", "development");
            return switch (env.toLowerCase(java.util.Locale.ROOT)) {
                case "prod", "production" -> PRODUCTION;
                case "staging", "stage" -> STAGING;
                default -> DEVELOPMENT;
            };
        }
    }
}
