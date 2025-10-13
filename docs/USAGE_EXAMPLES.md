# Open Payments Java SDK - Usage Examples

This document provides comprehensive code examples for common Open Payments operations.

## Table of Contents

- [Authentication & Setup](#authentication--setup)
- [Grant Management](#grant-management)
- [Token Lifecycle](#token-lifecycle)
- [HTTP Interceptors](#http-interceptors)
- [Error Handling](#error-handling)

---

## Authentication & Setup

### Generating Client Keys

```java
import zm.hashcode.openpayments.auth.keys.ClientKey;
import zm.hashcode.openpayments.auth.keys.ClientKeyGenerator;

// Generate a new Ed25519 key pair
ClientKey clientKey = ClientKeyGenerator.generate("my-key-id");

// Get the public key JWK for registration
Map<String, Object> publicJwk = clientKey.jwk();
System.out.println("Register this JWK with the authorization server:");
System.out.println(new ObjectMapper().writeValueAsString(publicJwk));

// Store private key securely (e.g., encrypted file, HSM)
// DO NOT commit private keys to version control!
```

### HTTP Signature Service

```java
import zm.hashcode.openpayments.auth.signature.HttpSignatureService;
import zm.hashcode.openpayments.http.core.HttpRequest;

// Create signature service
HttpSignatureService signatureService = new HttpSignatureService(clientKey);

// Sign an HTTP request
HttpRequest request = HttpRequest.builder()
    .method(HttpMethod.POST)
    .uri("https://auth.example.com/gnap")
    .headers(Map.of("Content-Type", "application/json"))
    .body(requestBody)
    .build();

HttpRequest signedRequest = signatureService.signRequest(request);

// Signed request now has Signature and Signature-Input headers
```

---

## Grant Management

### Requesting a Grant

```java
import zm.hashcode.openpayments.auth.grant.*;

// Create grant service
ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new com.fasterxml.jackson.datatype.jdk8.Jdk8Module());

GrantService grantService = new GrantService(
    httpClient,
    signatureService,
    objectMapper
);

// Build grant request
GrantRequest request = GrantRequest.builder()
    .accessToken(AccessTokenRequest.builder()
        .access(Access.incomingPayment(List.of("create", "read", "complete")))
        .access(Access.quote(List.of("create", "read")))
        .build())
    .client(Client.builder()
        .key(clientKey.jwk())
        .build())
    .interact(Interact.redirect("https://example.com/callback"))
    .build();

// Request grant
GrantResponse response = grantService
    .requestGrant("https://auth.example.com/", request)
    .join();

if (response.requiresInteraction()) {
    // User interaction required
    String redirectUrl = response.interact().get().redirect().get().uri();
    System.out.println("Redirect user to: " + redirectUrl);

    // After user interaction, continue the grant
    String interactRef = "user-provided-interact-ref";
    GrantResponse finalResponse = grantService
        .continueGrant(response.continuation().get(), interactRef)
        .join();

    AccessTokenResponse token = finalResponse.accessToken().get();
    System.out.println("Access token: " + token.value());

} else if (response.isApproved()) {
    // Grant approved immediately
    AccessTokenResponse token = response.accessToken().get();
    System.out.println("Access token: " + token.value());
}
```

### Factory Methods for Common Access Types

```java
// Incoming payment access
Access incomingPayment = Access.incomingPayment(List.of("create", "read", "complete"));

// Outgoing payment access
Access outgoingPayment = Access.outgoingPayment(List.of("create", "read"));

// Quote access
Access quote = Access.quote(List.of("create", "read"));

// With resource limits
Access limitedAccess = Access.incomingPayment(
    List.of("create"),
    Optional.of("https://wallet.example.com/alice/incoming-payments/abc123"),
    Optional.of(Limits.builder()
        .receiveAmount(Amount.of("1000", "USD", 2))
        .interval("P1M") // One month
        .build())
);
```

### Canceling a Grant

```java
// Cancel an in-progress grant
grantService.cancelGrant(response.continuation().get()).join();
System.out.println("Grant canceled");
```

---

## Token Lifecycle

### Rotating Tokens

```java
import zm.hashcode.openpayments.auth.token.TokenManager;

TokenManager tokenManager = new TokenManager(httpClient, objectMapper);

// Check if token is expiring soon
AccessTokenResponse currentToken = response.accessToken().get();
if (currentToken.expiresIn().isPresent()) {
    long expiresIn = currentToken.expiresIn().get();

    if (expiresIn < 300) { // Less than 5 minutes
        // Rotate token to get a new one
        AccessTokenResponse newToken = tokenManager
            .rotateToken(currentToken)
            .join();

        System.out.println("Token rotated successfully");
        System.out.println("New token expires in: " + newToken.expiresIn().get() + " seconds");

        // Use newToken for subsequent requests
    }
}
```

### Revoking Tokens

```java
// Revoke token when done
tokenManager.revokeToken(token).join();
System.out.println("Token revoked");

// Attempting to use revoked token will result in 401 Unauthorized
```

### Token Management Best Practices

```java
public class TokenRefreshScheduler {
    private final TokenManager tokenManager;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile AccessTokenResponse currentToken;

    public void scheduleRefresh(AccessTokenResponse initialToken) {
        this.currentToken = initialToken;

        initialToken.expiresIn().ifPresent(expiresIn -> {
            // Refresh token when 80% of lifetime has passed
            long refreshDelay = (long) (expiresIn * 0.8);

            scheduler.schedule(() -> {
                try {
                    AccessTokenResponse newToken = tokenManager
                        .rotateToken(currentToken)
                        .join();

                    this.currentToken = newToken;

                    // Schedule next refresh
                    scheduleRefresh(newToken);

                } catch (Exception e) {
                    System.err.println("Token rotation failed: " + e.getMessage());
                }
            }, refreshDelay, TimeUnit.SECONDS);
        });
    }

    public AccessTokenResponse getCurrentToken() {
        return currentToken;
    }

    public void shutdown() {
        // Revoke token before shutdown
        tokenManager.revokeToken(currentToken).join();
        scheduler.shutdown();
    }
}
```

---

## HTTP Interceptors

### Request Logging

```java
import zm.hashcode.openpayments.http.interceptor.LoggingRequestInterceptor;
import java.util.logging.Level;

// Basic request logging (INFO level, includes headers)
client.addRequestInterceptor(new LoggingRequestInterceptor());

// Custom configuration
LoggingRequestInterceptor customLogging = new LoggingRequestInterceptor(
    Level.FINE,    // Log level
    true,          // Log headers
    false          // Don't log body (may contain sensitive data)
);
client.addRequestInterceptor(customLogging);

// Output example:
// INFO: HTTP Request: POST https://auth.example.com/gnap
// Headers:
//   Content-Type: application/json
//   Authorization: ***REDACTED***
//   Signature: ***REDACTED***
```

### Response Logging

```java
import zm.hashcode.openpayments.http.interceptor.LoggingResponseInterceptor;

// Basic response logging
client.addResponseInterceptor(new LoggingResponseInterceptor());

// Custom configuration
LoggingResponseInterceptor customLogging = new LoggingResponseInterceptor(
    Level.INFO,      // Success log level
    Level.WARNING,   // Error log level
    true,            // Log headers
    true             // Log body
);
client.addResponseInterceptor(customLogging);

// Output example for success:
// INFO: HTTP Response: 200 OK
// Headers:
//   Content-Type: application/json
// Body: {"access_token": {...}}

// Output example for error:
// WARNING: HTTP Response: 401 Client Error
// Headers:
//   Content-Type: application/json
// Body: {"error": "unauthorized", "message": "Invalid token"}
```

### Authentication Interceptor

```java
import zm.hashcode.openpayments.http.interceptor.AuthenticationInterceptor;

// OAuth 2.0 / JWT Bearer tokens
String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
client.addRequestInterceptor(AuthenticationInterceptor.bearer(accessToken));

// GNAP tokens (Open Payments)
String gnapToken = "OS9M2PMHKUR64TB8N6BW7OZB8CDFONP219RP1LT0";
client.addRequestInterceptor(AuthenticationInterceptor.gnap(gnapToken));

// HTTP Basic authentication
String credentials = Base64.getEncoder()
    .encodeToString("username:password".getBytes());
client.addRequestInterceptor(AuthenticationInterceptor.basic(credentials));

// Custom authentication scheme
client.addRequestInterceptor(
    AuthenticationInterceptor.custom("ApiKey", "sk_live_1234567890")
);
```

### Error Handling Interceptor

```java
import zm.hashcode.openpayments.http.interceptor.ErrorHandlingInterceptor;

ErrorHandlingInterceptor errorHandler = new ErrorHandlingInterceptor(objectMapper);
client.addResponseInterceptor(errorHandler);

// Automatically extracts and logs structured error information
// from JSON responses:
//
// WARNING: HTTP Error Response: 400 - Invalid request
// WARNING: Error Code: invalid_grant
// FINE: Error Details: The grant request is missing required fields

// Supports multiple error formats:
// - {"error": "...", "error_description": "..."}
// - {"message": "...", "code": "...", "details": "..."}
// - {"title": "...", "detail": "...", "type": "..."}
```

### Combining Interceptors

```java
// Create HTTP client
HttpClient client = new ApacheHttpClient(config);

// Add interceptors in order (they execute in sequence)

// 1. Request logging (before authentication)
client.addRequestInterceptor(new LoggingRequestInterceptor(
    Level.FINE, true, false
));

// 2. Authentication (adds headers after logging)
client.addRequestInterceptor(AuthenticationInterceptor.gnap(token));

// 3. Error handling (processes error responses)
client.addResponseInterceptor(new ErrorHandlingInterceptor(objectMapper));

// 4. Response logging (logs after error handling)
client.addResponseInterceptor(new LoggingResponseInterceptor(
    Level.INFO, Level.SEVERE, true, true
));

// Use client as normal - interceptors work transparently
HttpResponse response = client.execute(request).join();
```

### Custom Interceptors

```java
// Rate limit tracking interceptor
ResponseInterceptor rateLimitTracker = response -> {
    response.getHeader("X-RateLimit-Remaining")
        .ifPresent(remaining -> {
            int count = Integer.parseInt(remaining);
            if (count < 10) {
                System.out.println("WARNING: Only " + count + " requests remaining");
            }
        });
    return response;
};
client.addResponseInterceptor(rateLimitTracker);

// Request ID interceptor
RequestInterceptor requestIdInjector = request -> {
    String requestId = UUID.randomUUID().toString();

    Map<String, String> newHeaders = new HashMap<>(request.headers());
    newHeaders.put("X-Request-ID", requestId);

    return HttpRequest.builder()
        .method(request.method())
        .uri(request.uri())
        .headers(newHeaders)
        .body(request.getBody().orElse(null))
        .build();
};
client.addRequestInterceptor(requestIdInjector);
```

---

## Error Handling

### Grant Errors

```java
try {
    GrantResponse response = grantService.requestGrant(authServerUrl, request).join();
    // Process response

} catch (CompletionException e) {
    if (e.getCause() instanceof GrantException) {
        GrantException ge = (GrantException) e.getCause();
        System.err.println("Grant failed: " + ge.getMessage());

        // Handle specific error cases
        if (ge.getMessage().contains("401")) {
            System.err.println("Authentication failed - check client key");
        } else if (ge.getMessage().contains("403")) {
            System.err.println("Insufficient permissions");
        }
    }
}
```

### Token Errors

```java
try {
    AccessTokenResponse newToken = tokenManager.rotateToken(currentToken).join();

} catch (CompletionException e) {
    if (e.getCause() instanceof TokenException) {
        TokenException te = (TokenException) e.getCause();

        if (te.getMessage().contains("401")) {
            // Token is no longer valid - need to request new grant
            System.err.println("Token rotation failed: token is invalid");
        } else if (te.getMessage().contains("parse")) {
            // Server returned unexpected response format
            System.err.println("Failed to parse token response");
        }
    }
}
```

### Retry Logic with Resilient HTTP Client

```java
import zm.hashcode.openpayments.http.resilience.*;

// Configure retry behavior
ResilienceConfig resilienceConfig = ResilienceConfig.builder()
    .maxRetries(3)
    .retryStrategy(RetryStrategy.exponentialBackoff(
        Duration.ofSeconds(1),  // Base delay
        2.0                      // Multiplier
    ))
    .retryableStatusCodes(Set.of(429, 502, 503, 504))
    .circuitBreakerEnabled(true)
    .circuitBreakerThreshold(5)
    .circuitBreakerTimeout(Duration.ofMinutes(1))
    .build();

// Wrap base client with resilience
HttpClient baseClient = new ApacheHttpClient(clientConfig);
HttpClient resilientClient = new ResilientHttpClient(baseClient, resilienceConfig);

// Use resilient client - automatically retries on transient failures
HttpResponse response = resilientClient.execute(request).join();
```

---

## Complete Example: Payment Flow

```java
public class PaymentExample {
    public static void main(String[] args) throws Exception {
        // 1. Setup
        ClientKey clientKey = ClientKeyGenerator.generate("my-client-key");
        HttpSignatureService signatureService = new HttpSignatureService(clientKey);

        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jdk8.Jdk8Module());

        // 2. Configure HTTP client with interceptors
        HttpClient httpClient = new ApacheHttpClient(HttpClientConfig.defaultConfig());
        httpClient.addRequestInterceptor(new LoggingRequestInterceptor());
        httpClient.addResponseInterceptor(new ErrorHandlingInterceptor(objectMapper));

        // 3. Create services
        GrantService grantService = new GrantService(httpClient, signatureService, objectMapper);
        TokenManager tokenManager = new TokenManager(httpClient, objectMapper);

        try {
            // 4. Request grant for incoming payment access
            GrantRequest grantRequest = GrantRequest.builder()
                .accessToken(AccessTokenRequest.builder()
                    .access(Access.incomingPayment(List.of("create", "read")))
                    .build())
                .client(Client.builder()
                    .key(clientKey.jwk())
                    .build())
                .build();

            GrantResponse grantResponse = grantService
                .requestGrant("https://auth.example.com/", grantRequest)
                .join();

            if (!grantResponse.isApproved()) {
                System.err.println("Grant not approved");
                return;
            }

            AccessTokenResponse token = grantResponse.accessToken().get();
            System.out.println("Received access token: " + token.value());

            // 5. Use token for payment operations
            // (Add authentication interceptor with token)
            httpClient.addRequestInterceptor(AuthenticationInterceptor.gnap(token.value()));

            // ... perform payment operations ...

            // 6. Rotate token before expiration
            if (token.expiresIn().isPresent() && token.expiresIn().get() < 300) {
                AccessTokenResponse newToken = tokenManager.rotateToken(token).join();
                System.out.println("Token rotated");
                token = newToken;
            }

            // 7. Cleanup
            tokenManager.revokeToken(token).join();
            System.out.println("Token revoked");

        } finally {
            httpClient.close();
        }
    }
}
```

---

For more information, see:
- [Open Payments Documentation](https://openpayments.dev)
- [GNAP Protocol Specification](https://datatracker.ietf.org/doc/html/rfc9635)
- [HTTP Signatures Specification](https://datatracker.ietf.org/doc/html/rfc9421)
