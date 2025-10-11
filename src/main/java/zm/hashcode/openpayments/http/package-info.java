/**
 * HTTP client abstraction layer with built-in resilience features.
 *
 * <p>
 * This package provides a flexible, library-agnostic HTTP client abstraction that can be easily swapped between
 * different HTTP library implementations (Apache HttpClient, OkHttp, JDK HttpClient, etc.) without affecting the rest
 * of the application.
 *
 * <h2>Package Structure</h2>
 *
 * <p>
 * The HTTP module is organized into logical subpackages:
 *
 * <pre>
 * http/
 * ├── core/          Core abstractions (HttpClient, HttpRequest, HttpResponse, HttpMethod)
 * ├── config/        Configuration classes (HttpClientConfig, HttpClientImplementation)
 * ├── resilience/    Resilience features (ResilienceConfig, RetryStrategy, ResilientHttpClient)
 * ├── interceptor/   Interceptor interfaces (RequestInterceptor, ResponseInterceptor)
 * ├── impl/          Concrete implementations (ApacheHttpClient, OkHttpClientImpl)
 * └── factory/       Factory and builder classes (HttpClientBuilder, HttpClientFactory)
 * </pre>
 *
 * <h2>Architecture</h2>
 *
 * <p>
 * The HTTP client layer follows a layered architecture with clear separation of concerns:
 *
 * <pre>
 * ┌─────────────────────────────────────────────────────┐
 * │         Application Code (Services)                 │
 * └─────────────────────────────────────────────────────┘
 *                         ▼
 * ┌─────────────────────────────────────────────────────┐
 * │         HttpClient Interface (core)                 │
 * └─────────────────────────────────────────────────────┘
 *                         ▼
 * ┌─────────────────────────────────────────────────────┐
 * │      ResilientHttpClient (resilience)               │
 * │  - Retries with configurable backoff                │
 * │  - Circuit breaker pattern                          │
 * │  - Virtual thread-based delays                      │
 * └─────────────────────────────────────────────────────┘
 *                         ▼
 * ┌─────────────────────────────────────────────────────┐
 * │    Implementation (impl)                            │
 * │  - Library-specific HTTP operations                 │
 * │  - Connection pooling                               │
 * │  - Base URL resolution                              │
 * └─────────────────────────────────────────────────────┘
 * </pre>
 *
 * <h2>Key Components by Package</h2>
 *
 * <h3>{@link zm.hashcode.openpayments.http.core} - Core Abstractions</h3>
 * <ul>
 * <li>{@link zm.hashcode.openpayments.http.core.HttpClient} - Main HTTP client interface</li>
 * <li>{@link zm.hashcode.openpayments.http.core.HttpRequest} - Immutable request representation</li>
 * <li>{@link zm.hashcode.openpayments.http.core.HttpResponse} - Immutable response representation</li>
 * <li>{@link zm.hashcode.openpayments.http.core.HttpMethod} - HTTP method enumeration</li>
 * </ul>
 *
 * <h3>{@link zm.hashcode.openpayments.http.config} - Configuration</h3>
 * <ul>
 * <li>{@link zm.hashcode.openpayments.http.config.HttpClientConfig} - HTTP client configuration (timeouts, pooling,
 * SSL)</li>
 * <li>{@link zm.hashcode.openpayments.http.config.HttpClientImplementation} - Implementation selection enum</li>
 * </ul>
 *
 * <h3>{@link zm.hashcode.openpayments.http.resilience} - Resilience Layer</h3>
 * <ul>
 * <li>{@link zm.hashcode.openpayments.http.resilience.ResilienceConfig} - Resilience configuration (retries, circuit
 * breaker)</li>
 * <li>{@link zm.hashcode.openpayments.http.resilience.RetryStrategy} - Retry delay calculation strategies</li>
 * <li>{@link zm.hashcode.openpayments.http.resilience.ResilientHttpClient} - Decorator adding retry and circuit breaker
 * logic</li>
 * </ul>
 *
 * <h3>{@link zm.hashcode.openpayments.http.interceptor} - Interceptors</h3>
 * <ul>
 * <li>{@link zm.hashcode.openpayments.http.interceptor.RequestInterceptor} - Request modification/logging</li>
 * <li>{@link zm.hashcode.openpayments.http.interceptor.ResponseInterceptor} - Response modification/logging</li>
 * </ul>
 *
 * <h3>{@link zm.hashcode.openpayments.http.impl} - Implementations</h3>
 * <ul>
 * <li>{@link zm.hashcode.openpayments.http.impl.ApacheHttpClient} - Apache HttpClient 5 implementation</li>
 * <li>{@link zm.hashcode.openpayments.http.impl.OkHttpClientImpl} - OkHttp implementation</li>
 * </ul>
 *
 * <h3>{@link zm.hashcode.openpayments.http.factory} - Factory</h3>
 * <ul>
 * <li>{@link zm.hashcode.openpayments.http.factory.HttpClientBuilder} - Fluent builder for creating configured
 * clients</li>
 * <li>{@link zm.hashcode.openpayments.http.factory.HttpClientFactory} - Advanced factory with named instances</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Usage</h3>
 *
 * <pre>{@code
 * import zm.hashcode.openpayments.http.factory.HttpClientBuilder;
 * import zm.hashcode.openpayments.http.core.*;
 *
 * // Create a simple client
 * HttpClient client = HttpClientBuilder.simple("https://api.example.com");
 *
 * // Make a request
 * var request = HttpRequest.builder().method(HttpMethod.GET).uri("/users/123").header("Accept", "application/json")
 *         .build();
 *
 * // Execute
 * HttpResponse response = client.execute(request).join();
 * }</pre>
 *
 * <h3>Advanced Configuration</h3>
 *
 * <pre>{@code
 * import zm.hashcode.openpayments.http.factory.HttpClientBuilder;
 * import zm.hashcode.openpayments.http.config.HttpClientImplementation;
 * import zm.hashcode.openpayments.http.resilience.RetryStrategy;
 *
 * HttpClient client = HttpClientBuilder.create().baseUrl("https://api.example.com")
 *         .implementation(HttpClientImplementation.APACHE).connectTimeout(Duration.ofSeconds(10))
 *         .requestTimeout(Duration.ofSeconds(30)).maxRetries(3)
 *         .retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100))).circuitBreakerEnabled(true).build();
 * }</pre>
 *
 * <h3>Multiple Implementations</h3>
 *
 * <pre>{@code
 * import zm.hashcode.openpayments.http.factory.*;
 * import zm.hashcode.openpayments.http.config.HttpClientImplementation;
 *
 * // Fast internal service - OkHttp
 * HttpClient internal = HttpClientBuilder.create().baseUrl("http://internal-api:8080")
 *         .implementation(HttpClientImplementation.OKHTTP).maxRetries(1).build();
 *
 * // Slow external service - Apache with aggressive retries
 * HttpClient external = HttpClientBuilder.create().baseUrl("https://external-api.com")
 *         .implementation(HttpClientImplementation.APACHE).maxRetries(5).build();
 * }</pre>
 *
 * <h3>Factory Pattern</h3>
 *
 * <pre>{@code
 * import zm.hashcode.openpayments.http.factory.HttpClientFactory;
 *
 * // Register named clients
 * HttpClientFactory.register("payment",
 *     HttpClientBuilder.create()
 *         .baseUrl("https://payments.api.com")
 *         .implementation(HttpClientImplementation.APACHE)
 *         .maxRetries(5)
 *         .build());
 *
 * // Retrieve by name
 * HttpClient paymentClient = HttpClientFactory.get("payment");
 * }</pre>
 *
 * <h2>Design Principles</h2>
 *
 * <h3>1. Package by Feature</h3>
 * <p>
 * Classes are organized by their role/feature rather than by technical layer. This makes it easier to understand the
 * module's structure and find related classes.
 *
 * <h3>2. Clear Dependencies</h3>
 * <p>
 * Dependency direction: core ← config ← resilience ← impl ← factory
 *
 * <h3>3. Abstraction</h3>
 * <p>
 * The {@code core} package contains pure abstractions with no implementation dependencies, making it easy to swap
 * implementations.
 *
 * <h3>4. Separation of Concerns</h3>
 * <p>
 * Each package has a single, well-defined responsibility:
 * <ul>
 * <li><b>core</b> - Defines contracts</li>
 * <li><b>config</b> - Configuration and settings</li>
 * <li><b>resilience</b> - Retry and circuit breaker logic</li>
 * <li><b>interceptor</b> - Request/response interception</li>
 * <li><b>impl</b> - Concrete HTTP client implementations</li>
 * <li><b>factory</b> - Object creation and configuration</li>
 * </ul>
 *
 * <h3>5. Decorator Pattern</h3>
 * <p>
 * The {@code ResilientHttpClient} in the resilience package wraps any {@code HttpClient} implementation to add
 * resilience features, keeping concerns separated.
 *
 * <h2>Thread Safety</h2>
 * <p>
 * All components are thread-safe and can be shared across multiple threads. The {@code HttpClient} implementations
 * maintain internal connection pools that handle concurrent requests efficiently.
 *
 * @since 1.0
 */
package zm.hashcode.openpayments.http;
