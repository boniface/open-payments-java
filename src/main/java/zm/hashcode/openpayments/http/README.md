# HTTP Client Module

> **Part of**: [Open Payments Java SDK](../../../../../../README.md)
> **Package**: `zm.hashcode.openpayments.http`
> **Purpose**: Internal HTTP client abstraction layer with built-in resilience

---

## Overview

The `http` package provides a flexible, library-agnostic HTTP client abstraction layer used internally by the Open Payments Java SDK. It enables the SDK to communicate with Open Payments API endpoints while protecting the codebase from changes to underlying HTTP libraries.

**This is an internal module** - SDK users typically interact with the high-level client API (see [Quick Start](../../../../../../README.md#quick-start)) rather than using this HTTP layer directly.

## Why This Module Exists

The Open Payments Java SDK needs to make HTTP requests to various API endpoints. Rather than coupling the entire SDK to a specific HTTP library (Apache HttpClient, OkHttp, etc.), this module:

1. **Abstracts HTTP operations** - Rest of SDK depends only on interfaces (`HttpClient`, `HttpRequest`, `HttpResponse`)
2. **Enables library swapping** - Easily switch between Apache HttpClient, OkHttp, JDK HttpClient, etc.
3. **Provides resilience** - Built-in retries, circuit breaker, and backoff strategies
4. **Leverages Java 25** - Uses virtual threads for efficient async operations

## Package Structure

Following "package by feature" organization:

```
http/
├── core/              # Core abstractions
│   ├── HttpClient.java          # Main HTTP client interface
│   ├── HttpRequest.java         # Immutable request model (record)
│   ├── HttpResponse.java        # Immutable response model (record)
│   └── HttpMethod.java          # HTTP method enumeration
│
├── config/            # Configuration
│   ├── HttpClientConfig.java        # HTTP client settings
│   └── HttpClientImplementation.java # Implementation selector
│
├── resilience/        # Resilience features
│   ├── ResilienceConfig.java    # Resilience settings
│   ├── RetryStrategy.java       # Retry delay strategies
│   └── ResilientHttpClient.java # Retry/circuit breaker decorator
│
├── interceptor/       # Request/Response interceptors
│   ├── RequestInterceptor.java  # Request modification interface
│   └── ResponseInterceptor.java # Response processing interface
│
├── impl/              # Concrete implementations
│   ├── ApacheHttpClient.java    # Apache HttpClient 5 (default)
│   └── OkHttpClientImpl.java    # OkHttp alternative
│
└── factory/           # Object creation
    ├── HttpClientBuilder.java   # Fluent builder API
    └── HttpClientFactory.java   # Named instances & advanced patterns
```

## Key Features

### Swappable HTTP Libraries

Choose between different HTTP client implementations:

```java
// Apache HttpClient 5 (default, recommended for production)
HttpClient client = HttpClientBuilder.create()
    .baseUrl("https://wallet.example.com")
    .implementation(HttpClientImplementation.APACHE)
    .build();

// OkHttp (lightweight, good for Android)
HttpClient client = HttpClientBuilder.create()
    .baseUrl("https://wallet.example.com")
    .implementation(HttpClientImplementation.OKHTTP)
    .build();

// Auto-detect (uses Apache if available, falls back to OkHttp)
HttpClient client = HttpClientBuilder.create()
    .baseUrl("https://wallet.example.com")
    .implementation(HttpClientImplementation.AUTO)
    .build();
```

### Built-in Resilience

Automatic retries with configurable strategies:

```java
HttpClient client = HttpClientBuilder.create()
    .baseUrl("https://wallet.example.com")
    .maxRetries(3)
    .retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100)))
    .circuitBreakerEnabled(true)
    .circuitBreakerThreshold(5)
    .build();
```

### Base URL Abstraction

Configure once, use everywhere:

```java
HttpClient client = HttpClientBuilder.simple("https://wallet.example.com");

// Relative URIs automatically resolved
var request = HttpRequest.builder()
    .method(HttpMethod.GET)
    .uri("/alice")  // → https://wallet.example.com/alice
    .build();

// Absolute URIs used as-is
var request2 = HttpRequest.builder()
    .method(HttpMethod.POST)
    .uri("https://auth.example.com/token")  // Used as-is
    .build();
```

### Virtual Threads (Java 25)

Efficient async operations using virtual threads:

```java
// Non-blocking retry delays
CompletableFuture<HttpResponse> future = client.execute(request);

// Thousands of concurrent requests without blocking platform threads
List<CompletableFuture<HttpResponse>> futures = walletAddresses.stream()
    .map(addr -> client.execute(createRequest(addr)))
    .toList();
```

## Usage Examples

### Basic Request

```java
import zm.hashcode.openpayments.http.factory.HttpClientBuilder;
import zm.hashcode.openpayments.http.core.*;

// Create client
HttpClient client = HttpClientBuilder.simple("https://wallet.example.com");

// Build request
var request = HttpRequest.builder()
    .method(HttpMethod.GET)
    .uri("/alice")
    .header("Accept", "application/json")
    .build();

// Execute (returns CompletableFuture)
HttpResponse response = client.execute(request).join();

System.out.println("Status: " + response.statusCode());
System.out.println("Body: " + response.getBody().orElse(""));
```

### POST with JSON Body

```java
var request = HttpRequest.builder()
    .method(HttpMethod.POST)
    .uri("/incoming-payments")
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer " + token)
    .body("{\"walletAddress\":\"https://wallet.example.com/alice\"}")
    .build();

var response = client.execute(request).join();
```

### Advanced Configuration

```java
import zm.hashcode.openpayments.http.config.HttpClientImplementation;
import zm.hashcode.openpayments.http.resilience.RetryStrategy;

HttpClient client = HttpClientBuilder.create()
    .baseUrl("https://wallet.example.com")
    .implementation(HttpClientImplementation.APACHE)

    // Timeouts
    .connectTimeout(Duration.ofSeconds(10))
    .requestTimeout(Duration.ofSeconds(30))
    .socketTimeout(Duration.ofSeconds(30))

    // Connection pooling
    .maxConnections(100)
    .maxConnectionsPerRoute(20)
    .connectionTimeToLive(Duration.ofMinutes(5))

    // Resilience
    .maxRetries(3)
    .retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100))
        .withFullJitter())
    .circuitBreakerEnabled(true)
    .circuitBreakerThreshold(5)
    .circuitBreakerTimeout(Duration.ofMinutes(1))

    .build();
```

## Retry Strategies

### Exponential Backoff (Recommended)

```java
var strategy = RetryStrategy.exponentialBackoff(Duration.ofMillis(100));
// Attempt 1: 100ms, Attempt 2: 200ms, Attempt 3: 400ms, Attempt 4: 800ms...
```

### With Jitter (Prevents Thundering Herd)

```java
// Full jitter: random(0, delay)
var strategy = RetryStrategy.exponentialBackoff(Duration.ofMillis(100))
    .withFullJitter();

// Equal jitter: delay/2 + random(0, delay/2)
var strategy = RetryStrategy.exponentialBackoff(Duration.ofMillis(100))
    .withEqualJitter();

// Decorrelated jitter (AWS recommended)
var strategy = RetryStrategy.decorrelatedJitter(Duration.ofMillis(100));
```

### Other Strategies

```java
// Fixed delay
RetryStrategy.fixedDelay(Duration.ofMillis(500));

// Linear backoff
RetryStrategy.linearBackoff(Duration.ofMillis(100));
```

## Circuit Breaker

Protects against cascading failures by failing fast when a service is unhealthy:

```
CLOSED (normal) → requests pass through
   ↓ (failures ≥ threshold)
OPEN (failing fast) → fail immediately without attempting request
   ↓ (timeout elapsed)
HALF_OPEN (testing) → allow limited requests to test recovery
   ↓ (success)
CLOSED (recovered)
```

Configuration:

```java
.circuitBreakerEnabled(true)
.circuitBreakerThreshold(5)           // Open after 5 consecutive failures
.circuitBreakerTimeout(Duration.ofMinutes(1))  // Stay open for 1 minute
.circuitBreakerHalfOpenRequests(3)    // Test with 3 requests in half-open
```

## Implementation Selection

### Available Implementations

The HTTP module supports multiple HTTP client libraries. Choose the best one for your use case:

#### Apache HttpClient 5 (Recommended for Production)
```java
HttpClient client = HttpClientBuilder.create()
    .baseUrl("https://wallet.example.com")
    .implementation(HttpClientImplementation.APACHE)
    .build();
```

**Best for:**
- Production servers and enterprise applications
- Java 21+ applications (optimized for virtual threads)
- High-throughput, long-running server processes
- Complex HTTP requirements

**Characteristics:**
- Mature and battle-tested (20+ years)
- Full HTTP/2 support
- Advanced connection pooling
- Larger dependency (~500KB)

#### OkHttp (Lightweight Alternative)
```java
HttpClient client = HttpClientBuilder.create()
    .baseUrl("https://wallet.example.com")
    .implementation(HttpClientImplementation.OKHTTP)
    .build();
```

**Best for:**
- Android and mobile applications
- Memory-constrained environments
- Applications requiring WebSocket support
- Scenarios where built-in caching is beneficial

**Characteristics:**
- Lightweight and fast
- Default HTTP client for Android
- Built-in response caching and WebSocket support
- Smaller dependency (~250KB)

#### Auto-detect (Default)
```java
HttpClient client = HttpClientBuilder.create()
    .baseUrl("https://wallet.example.com")
    .implementation(HttpClientImplementation.AUTO)  // or omit - it's the default
    .build();
```

**Selection priority:** Apache → OkHttp → throws exception

### Implementation Selection Strategies

#### Single Global Implementation
Use the same implementation throughout the SDK:

```java
// At SDK initialization
var client = OpenPaymentsClient.builder()
    .walletAddress("https://wallet.example.com/alice")
    .privateKey(privateKey)
    .keyId(keyId)
    .httpImplementation(HttpClientImplementation.APACHE)
    .build();
```

#### Service-Specific Implementation
Different implementations for different use cases:

```java
// Fast internal service - OkHttp
HttpClient internal = HttpClientBuilder.create()
    .baseUrl("http://internal-api:8080")
    .implementation(HttpClientImplementation.OKHTTP)
    .maxRetries(1)
    .build();

// Critical external service - Apache with aggressive retries
HttpClient external = HttpClientBuilder.create()
    .baseUrl("https://external-api.com")
    .implementation(HttpClientImplementation.APACHE)
    .maxRetries(5)
    .retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100)))
    .build();
```

#### Environment-Specific Implementation
```java
String env = System.getenv("APP_ENV");
HttpClientImplementation impl = switch (env) {
    case "production" -> HttpClientImplementation.APACHE;
    case "staging" -> HttpClientImplementation.APACHE;
    default -> HttpClientImplementation.OKHTTP;  // development
};
```

### Quick Comparison

| Feature | Apache HttpClient 5 | OkHttp                      |
|---------|-------------------|-----------------------------|
| Maturity | 20+ years | 10+ years                   |
| Size | ~500KB | ~250KB                      |
| HTTP/2 | ✅ Full | ✅ Full                      |
| Virtual Threads | ✅ Optimized | ⚠️ Works(should align soon) |
| WebSocket | ❌ No | ✅ Yes                       |
| Built-in Caching | ❌ No | ✅ Yes                       |
| Best For | Production servers | Android/Mobile              |

### Verification

Check which implementations are available:

```bash
./gradlew verifyHttpImplementations
```

Output:
```
═══════════════════════════════════════════════════════════
  HTTP Client Implementation Verification
═══════════════════════════════════════════════════════════
  APACHE     ✓ Available     (Recommended for production)
  OKHTTP     ✓ Available     (Lightweight alternative)
───────────────────────────────────────────────────────────
```

## Request/Response Interceptors

Add cross-cutting concerns like logging, authentication, etc.:

```java
// Logging interceptor
client.addRequestInterceptor(request -> {
    System.out.println("→ " + request.method() + " " + request.uri());
    return request;
});

client.addResponseInterceptor(response -> {
    System.out.println("← " + response.statusCode());
    return response;
});

// Authentication interceptor
client.addRequestInterceptor(request ->
    HttpRequest.builder()
        .method(request.method())
        .uri(request.uri())
        .headers(request.headers())
        .header("Authorization", "Bearer " + getAccessToken())
        .body(request.body())
        .build()
);
```

## Configuration Reference

### HttpClientConfig Defaults

| Property | Default | Description |
|----------|---------|-------------|
| `connectTimeout` | 10s | Connection establishment timeout |
| `requestTimeout` | 30s | Complete request/response timeout |
| `socketTimeout` | 30s | Socket read timeout |
| `maxConnections` | 100 | Maximum total connections in pool |
| `maxConnectionsPerRoute` | 20 | Maximum connections per route |
| `connectionTimeToLive` | 5m | Time-to-live for pooled connections |
| `followRedirects` | true | Follow HTTP redirects automatically |

### ResilienceConfig Defaults

| Property | Default | Description |
|----------|---------|-------------|
| `maxRetries` | 3 | Maximum retry attempts |
| `retryStrategy` | Exponential (100ms) | Delay calculation strategy |
| `maxRetryDelay` | 30s | Maximum delay between retries |
| `retryOnStatusCodes` | 408, 429, 500, 502, 503, 504 | HTTP codes triggering retry |
| `circuitBreakerEnabled` | true | Enable circuit breaker |
| `circuitBreakerThreshold` | 5 | Failures before opening circuit |
| `circuitBreakerTimeout` | 1m | How long circuit stays open |

## Design Patterns

This module implements several design patterns for maintainability:

1. **Strategy Pattern** - `RetryStrategy` for pluggable backoff algorithms
2. **Decorator Pattern** - `ResilientHttpClient` wraps base client with resilience
3. **Builder Pattern** - `HttpClientBuilder` for fluent configuration
4. **Factory Pattern** - `HttpClientFactory` for advanced instance management
5. **Interface Segregation** - Small, focused interfaces (`RequestInterceptor`, `ResponseInterceptor`)

## Thread Safety

All components are thread-safe:
- **Immutable models** - `HttpRequest`, `HttpResponse`, configs use Java records
- **Atomic operations** - Circuit breaker uses `AtomicInteger`, `AtomicReference`
- **Connection pooling** - Managed by underlying HTTP library (Apache HttpClient 5 / OkHttp)

## Integration with SDK

This module is used internally by all SDK services:

```java
// SDK services use this HTTP client internally
public class WalletAddressService {
    private final HttpClient httpClient;

    public WalletAddressService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<WalletAddress> get(String url) {
        var request = HttpRequest.builder()
            .method(HttpMethod.GET)
            .uri(url)
            .header("Accept", "application/json")
            .build();

        return httpClient.execute(request)
            .thenApply(this::parseWalletAddress);
    }
}
```

SDK users configure the HTTP client through `OpenPaymentsClient.builder()`:

```java
var client = OpenPaymentsClient.builder()
    .walletAddress("https://wallet.example.com/alice")
    .privateKey(privateKey)
    .keyId(keyId)
    // HTTP configuration options available here
    .connectTimeout(Duration.ofSeconds(10))
    .maxRetries(5)
    .build();
```

## Best Practices

### 1. Reuse HttpClient Instances
HTTP clients are thread-safe and maintain connection pools. Create once, use everywhere:

```java
// Good - single instance
public class Services {
    private static final HttpClient HTTP_CLIENT = HttpClientBuilder.simple("https://api.example.com");

    public static HttpClient getHttpClient() {
        return HTTP_CLIENT;
    }
}

// Avoid - creating new instances repeatedly
public void makeRequest() {
    HttpClient client = HttpClientBuilder.simple("https://api.example.com");  // Don't do this!
    // ...
}
```

### 2. Use Appropriate Timeouts
Balance responsiveness with reliability:

```java
// Production - generous timeouts
.connectTimeout(Duration.ofSeconds(10))
.requestTimeout(Duration.ofSeconds(30))
.socketTimeout(Duration.ofSeconds(30))

// Development - faster feedback
.connectTimeout(Duration.ofSeconds(5))
.requestTimeout(Duration.ofSeconds(10))
```

### 3. Enable Resilience in Production
Use retries and circuit breakers, but consider disabling in development:

```java
boolean isProduction = "production".equals(System.getenv("ENV"));

HttpClient client = HttpClientBuilder.create()
    .baseUrl(apiUrl)
    .resilienceEnabled(isProduction)
    .maxRetries(isProduction ? 5 : 0)
    .build();
```

### 4. Use Jitter with Retries
Prevents thundering herd effects:

```java
// Good - with jitter
.retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100))
    .withFullJitter())

// Avoid - without jitter (can cause thundering herd)
.retryStrategy(RetryStrategy.exponentialBackoff(Duration.ofMillis(100)))
```

### 5. Monitor Circuit Breaker State
Set up alerts when circuits open in production:

```java
client.addResponseInterceptor(response -> {
    if (response.statusCode() >= 500) {
        metrics.increment("circuit_breaker.failures");
    }
    return response;
});
```

### 6. Close Clients When Done
Release resources properly:

```java
try (HttpClient client = HttpClientBuilder.simple("https://api.example.com")) {
    // Use client
    var response = client.execute(request).join();
} // Automatically closed
```

### 7. Use Virtual Threads Effectively
Let the client handle concurrency:

```java
// Good - concurrent execution with virtual threads
List<CompletableFuture<HttpResponse>> futures = urls.stream()
    .map(url -> HttpRequest.builder().method(HttpMethod.GET).uri(url).build())
    .map(client::execute)
    .toList();

CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
```

### 8. Explicit Implementation Selection
Be explicit about which implementation you're using in production:

```java
// Good - explicit
.implementation(HttpClientImplementation.APACHE)

// Avoid in production - implicit
.build()  // Uses AUTO detection
```

## Performance Tips

1. **Tune connection pool size** - Adjust `maxConnections` and `maxConnectionsPerRoute` based on traffic
2. **Use async execution** - Don't block on `.join()` unless necessary
3. **Monitor retry rates** - High retry rates indicate upstream issues
4. **Profile timeout settings** - Too short = false failures, too long = wasted resources
5. **Leverage HTTP/2** - Both Apache and OkHttp support multiplexing

## ADR Compliance

This module follows project Architecture Decision Records:

- **[ADR-001](../../../../../../docs/ADR.md#adr-001-use-java-25-language-features)** - Java 25 features (records, virtual threads, pattern matching)
- **[ADR-003](../../../../../../docs/ADR.md#adr-003-use-completablefuture-for-async-operations)** - `CompletableFuture` for async operations
- **[ADR-004](../../../../../../docs/ADR.md#adr-004-use-interfaces-for-all-services)** - Interface-based design
- **[ADR-005](../../../../../../docs/ADR.md#adr-005-use-apache-httpclient-5-with-virtual-threads)** - Apache HttpClient 5 with virtual thread support
- **[ADR-006](../../../../../../docs/ADR.md#adr-006-use-builder-pattern-for-complex-objects)** - Builder pattern for configuration

## See Also

- [Main README](../../../../../../README.md) - SDK overview and quick start
- [Architecture Guide](../../../../../../docs/ARCHITECTURE.md) - Overall SDK architecture
- [API Coverage](../../../../../../docs/API_COVERAGE.md) - Complete API mapping
- [Project ADRs](../../../../../../docs/ADR.md) - All architecture decisions

---

**Module Version**: Aligned with SDK version 1.0-SNAPSHOT
**Last Updated**: 06/10/2025
**Maintainer**: Open Payments Java SDK Team
