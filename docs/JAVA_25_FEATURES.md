# Java 25 Features Used in Open Payments SDK

[📚 Back to Documentation Index](INDEX.md) | [🏠 Back to README](../README.md)

---

## Purpose

This document demonstrates **how** modern Java 25 features are applied in the SDK with concrete examples. For **why** these features were chosen, see [ADR.md](ADR.md).

## Records (JEP 395)

Records are used extensively for immutable data models, drastically reducing boilerplate code.

### Benefits
- **Immutability by default** - All fields are final
- **Automatic implementation** of `equals()`, `hashCode()`, and `toString()`
- **Compact canonical constructor** for validation
- **Pattern matching support** (future-ready)

### Examples in SDK

#### Simple Record
```java
public record Amount(String value, String assetCode, int assetScale) {
    // Compact constructor for validation
    public Amount {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(assetCode, "assetCode must not be null");
    }

    // Additional methods
    public BigDecimal toBigDecimal() {
        return new BigDecimal(value).movePointLeft(assetScale);
    }
}
```

#### Record with Defensive Copy
```java
public record PaginatedResult<T>(List<T> items, String cursor, boolean hasMore) {
    public PaginatedResult {
        items = List.copyOf(items); // Defensive copy in compact constructor
    }
}
```

#### Record with Optional Helper
```java
public record WalletAddress(
    URI id,
    String assetCode,
    int assetScale,
    URI authServer,
    URI resourceServer,
    String publicName
) {
    public Optional<String> getPublicName() {
        return Optional.ofNullable(publicName);
    }
}
```

#### Record with Builder Pattern
```java
public record HttpRequest(HttpMethod method, URI uri, Map<String, String> headers, String body) {
    public HttpRequest {
        Objects.requireNonNull(method);
        Objects.requireNonNull(uri);
        headers = Map.copyOf(headers);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        // Builder implementation
    }
}
```

## Modern Java Features

### Var (Local Variable Type Inference)
```java
var client = OpenPaymentsClient.builder()
    .walletAddress("https://wallet.example.com/alice")
    .build();

var queryString = queryParams.entrySet().stream()
    .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
    .collect(Collectors.joining("&"));
```

### Text Blocks (JEP 378)
```java
// Clean multi-line strings (future use for documentation, JSON examples)
var jsonExample = """
    {
        "id": "https://wallet.example.com/alice",
        "assetCode": "USD",
        "assetScale": 2
    }
    """;
```

### Switch Expressions (JEP 361)
```java
// Can be used for response handling (future implementation)
var message = switch (response.statusCode()) {
    case 200 -> "Success";
    case 404 -> "Not found";
    case 500 -> "Server error";
    default -> "Unknown status";
};
```

### Sealed Classes (JEP 409)
```java
// Future use for restricted type hierarchies
public sealed interface PaymentType
    permits IncomingPayment, OutgoingPayment {
}
```

### Pattern Matching for instanceof (JEP 394)
```java
// Cleaner type checking and casting
if (exception instanceof OpenPaymentsException ex) {
    log.error("API error: {}", ex.getStatusCode());
}
```

## Records vs Classes in the SDK

### Used Records For:
 **Immutable DTOs**: `Amount`, `WalletAddress`, `IncomingPayment`, etc.
 **HTTP Models**: `HttpRequest`, `HttpResponse`
 **Results**: `PaginatedResult<T>`
 **Configuration**: Simple value holders

### Used Classes For:
 **Exceptions**: `OpenPaymentsException` (extends `RuntimeException`)
 **Builders**: Nested static classes in records
 **Utilities**: `JsonMapper`, `UrlBuilder`, `Validators`
 **Interfaces**: All service interfaces

## Code Reduction Statistics

Using records reduced code by approximately:
- **70% reduction** in model classes
- **No manual `equals()`/`hashCode()`** needed
- **No manual getters** needed
- **Built-in `toString()`** for debugging

### Before (Traditional Class):
```java
public final class Amount {
    private final String value;
    private final String assetCode;
    private final int assetScale;

    public Amount(String value, String assetCode, int assetScale) {
        this.value = Objects.requireNonNull(value);
        this.assetCode = Objects.requireNonNull(assetCode);
        this.assetScale = assetScale;
    }

    public String getValue() { return value; }
    public String getAssetCode() { return assetCode; }
    public int getAssetScale() { return assetScale; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Amount amount = (Amount) o;
        return assetScale == amount.assetScale &&
               Objects.equals(value, amount.value) &&
               Objects.equals(assetCode, amount.assetCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, assetCode, assetScale);
    }

    @Override
    public String toString() {
        return "Amount{value='" + value + "', assetCode='" +
               assetCode + "', assetScale=" + assetScale + "}";
    }
}
```

### After (Record):
```java
public record Amount(String value, String assetCode, int assetScale) {
    public Amount {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(assetCode, "assetCode must not be null");
    }
}
```

## Optional for Null Safety

Using Optional to handle nullable values safely:

```java
public record WalletAddress(
    URI id,
    String assetCode,
    int assetScale,
    URI authServer,
    URI resourceServer,
    String publicName  // May be null
) {
    // Helper method returns Optional
    public Optional<String> getPublicName() {
        return Optional.ofNullable(publicName);
    }
}

// Usage
var wallet = client.walletAddresses().get(url).join();
wallet.getPublicName()
    .ifPresent(name -> System.out.println("Name: " + name));
```

## Functional Interfaces (@FunctionalInterface)

Clean callbacks for interceptors:

```java
@FunctionalInterface
public interface RequestInterceptor {
    HttpRequest intercept(HttpRequest request);
}

// Usage
client.builder()
    .addRequestInterceptor(request -> {
        // Add custom header
        return request.withHeader("X-Custom", "value");
    })
    .build();
```

## Consumer Pattern for Builders

Using `Consumer<Builder>` for fluent API:

```java
public interface IncomingPaymentService {
    CompletableFuture<IncomingPayment> create(
        Consumer<IncomingPaymentRequest.Builder> requestBuilder
    );
}

// Usage
var payment = service.create(req -> req
    .walletAddress(wallet)
    .incomingAmount(Amount.of("100", "USD", 2))
    .expiresAt(Instant.now().plus(Duration.ofDays(1)))
).join();
```

## Stream API for Collections

Modern collection processing:

```java
// UrlBuilder using streams
public String build() {
    if (queryParams.isEmpty()) {
        return baseUrl;
    }

    var queryString = queryParams.entrySet().stream()
        .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
        .collect(Collectors.joining("&"));

    return baseUrl + "?" + queryString;
}
```

## Java Time API

Modern date/time handling:

```java
public record IncomingPayment(
    URI id,
    URI walletAddress,
    Amount incomingAmount,
    Amount receivedAmount,
    boolean completed,
    Instant expiresAt,      // java.time.Instant
    Instant createdAt,
    Instant updatedAt,
    String metadata
) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public Duration timeUntilExpiry() {
        return Duration.between(Instant.now(), expiresAt);
    }
}
```

## Feature Adoption Summary

| Feature | Usage | Files Using |
|---------|-------|-------------|
| **Records** | Data models | 12 files (Amount, WalletAddress, etc.) |
| **var** | Type inference | Throughout codebase |
| **Optional** | Null safety | WalletAddress, Grant, models with nullable fields |
| **CompletableFuture** | Async ops | All service interfaces |
| **Functional Interfaces** | Callbacks | RequestInterceptor, ResponseInterceptor |
| **Text Blocks** | Multi-line strings | Future use in docs/examples |
| **Switch Expressions** | Future use | Error handling (planned) |
| **Sealed Classes** | Future use | Payment type hierarchy (planned) |
| **Pattern Matching** | Future use | Response parsing (planned) |
| **Virtual Threads** | Runtime support | Implicit via Java 25 runtime |

## Virtual Threads in Action

Java 25 virtual threads enable efficient concurrent payment operations:

```java
// Process 1000 concurrent payments efficiently
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var futures = IntStream.range(0, 1000)
        .mapToObj(i -> CompletableFuture.supplyAsync(() ->
            client.incomingPayments()
                .create(req -> req
                    .walletAddress(wallet)
                    .incomingAmount(Amount.of("100", "USD", 2)))
                .join(),
            executor
        ))
        .toList();

    var results = futures.stream()
        .map(CompletableFuture::join)
        .toList();

    System.out.println("Created " + results.size() + " payments");
}
```

## Code Comparison: Before vs After

### Traditional Class (Before Java 25)
**Lines of Code**: ~50 lines

```java
public final class Amount {
    private final String value;
    private final String assetCode;
    private final int assetScale;

    public Amount(String value, String assetCode, int assetScale) {
        this.value = Objects.requireNonNull(value);
        this.assetCode = Objects.requireNonNull(assetCode);
        this.assetScale = assetScale;
    }

    public String getValue() { return value; }
    public String getAssetCode() { return assetCode; }
    public int getAssetScale() { return assetScale; }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Amount amount = (Amount) o;
        return assetScale == amount.assetScale &&
               Objects.equals(value, amount.value) &&
               Objects.equals(assetCode, amount.assetCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, assetCode, assetScale);
    }

    @Override
    public String toString() {
        return "Amount{value='" + value + "', assetCode='" +
               assetCode + "', assetScale=" + assetScale + "}";
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(value).movePointLeft(assetScale);
    }
}
```

### Record (Java 25)
**Lines of Code**: ~15 lines (70% reduction!)

```java
public record Amount(String value, String assetCode, int assetScale) {
    public Amount {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(assetCode, "assetCode must not be null");
    }

    public static Amount of(String value, String assetCode, int assetScale) {
        return new Amount(value, assetCode, assetScale);
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(value).movePointLeft(assetScale);
    }
}
```

## Best Practices Applied

1. **Immutability First**: All records and collections are immutable
2. **Defensive Copying**: Use `List.copyOf()` / `Map.copyOf()` in compact constructors
3. **Validation Early**: Validate in compact constructors, not scattered
4. **Optional for Nullables**: Never return null, use Optional
5. **CompletableFuture for Async**: Consistent async API across all operations
6. **Functional Callbacks**: Use Consumer/Function for flexible APIs
7. **Type Inference Where Clear**: Use var when type is obvious

## Related Documentation

- **[ADR.md](ADR.md)**: Why we chose Java 25 and these features
- **[SDK_STRUCTURE.md](SDK_STRUCTURE.md)**: Where these features are used
- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)**: Practical usage examples
