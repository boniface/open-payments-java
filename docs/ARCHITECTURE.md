# Open Payments Java SDK - Architecture

[📚 Back to Documentation Index](INDEX.md) | [🏠 Back to README](../README.md)

---

## Overview

This document describes the runtime architecture, component interactions, and system design of the Open Payments Java SDK. For architecture decisions, see [ADR.md](ADR.md). For package organization, see [SDK_STRUCTURE.md](SDK_STRUCTURE.md).

## System Architecture

### Component Diagram

```mermaid
graph TD
    UserApp[User Application]
    
    subgraph Client["OpenPaymentsClient (Entry Point)"]
        WalletService[Wallet<br/>Service]
        IncomingService[Incoming<br/>Payment<br/>Service]
        OutgoingService[Outgoing<br/>Payment<br/>Service]
        GrantService[Grant<br/>Service]
    end
    

    subgraph HTTPLayer[HTTP Client Layer]
            direction TB
            
            RequestInt["Request Interceptors <br/>(Auth, Logging, etc.)"]
            ApacheHTTP["Apache HttpClient 5 <br/>(Connection Pool)"]
            ResponseInt["Response Interceptors <br/> (Parsing, Validation)"]
            RequestInt --> ApacheHTTP --> ResponseInt
    end
    
    subgraph APIServers[Open Payments API Servers]
        AuthServer[Authorization<br/>Server<br/>GNAP]
        ResourceServer[Resource<br/>Server<br/>Payments]
    end

    UserApp --> Client
    Client --> HTTPLayer

    HTTPLayer --> APIServers
    APIServers --> AuthServer
    APIServers --> ResourceServer

    style UserApp fill:#e1f5ff,color:#000
    style Client fill:#fff4e1,color:#000
    style HTTPLayer fill:#f0f0f0,color:#000
    style APIServers fill:#e8f5e9,color:#000
```

## Package Architecture

```mermaid
graph TD
    Root[zm.hashcode.openpayments/]

    Root --> Client[client/<br/>Client initialization<br/>and configuration]
    Root --> Auth[auth/<br/>GNAP authorization flow]
    Root --> Wallet[wallet/<br/>Wallet address discovery]
    Root --> Payment[payment/<br/>Payment operations]
    Root --> Model[model/<br/>Shared data models]
    Root --> HTTP[http/<br/>HTTP abstraction layer]
    Root --> Util[util/<br/>Cross-cutting utilities]

    Payment --> Incoming[incoming/]
    Payment --> Outgoing[outgoing/]
    Payment --> Quote[quote/]

    style Root fill:#e3f2fd,color:#000
    style Payment fill:#fff9c4,color:#000
```

## Component Responsibilities

### Client Layer

**Purpose**: Provide single entry point and service access

- `OpenPaymentsClient`: Factory for all services, manages lifecycle
- `OpenPaymentsClientBuilder`: Fluent configuration (wallet address, keys, timeouts)
- **Lifecycle**: Created via builder, closed via AutoCloseable

### Service Layer

**Purpose**: Expose domain operations as clean APIs

- Each service represents one API resource type
- Methods return CompletableFuture for async operations
- Stateless (thread-safe, reusable)
- Delegate HTTP details to HTTP layer

### HTTP Layer

**Purpose**: Abstract HTTP communication and authentication

- `HttpClient`: Interface for HTTP operations
- `HttpRequest`/`HttpResponse`: Immutable request/response records
- `RequestInterceptor`: Add authentication headers (HTTP signatures, bearer tokens)
- `ResponseInterceptor`: Parse responses, handle errors
- **Implementation**: Apache HttpClient 5 with connection pooling

### Model Layer

**Purpose**: Represent API data structures

- Immutable records for all DTOs
- Validation in compact constructors
- Jackson annotations for JSON mapping
- Builders for complex objects

## Runtime Flow Examples

### Payment Creation Flow

```mermaid
sequenceDiagram
    participant User as User Code
    participant Client as OpenPaymentsClient
    participant Service as IncomingPaymentService
    participant HTTP as HttpClient
    participant Interceptor as RequestInterceptors
    participant Apache as Apache HttpClient 5
    participant API as Open Payments API

    User->>Client: client.incomingPayments().create(...)
    Client->>Service: create()
    Service->>Service: Build HttpRequest
    Service->>Interceptor: Apply interceptors (add auth headers)
    Interceptor->>HTTP: execute()
    HTTP->>Apache: Send request
    Apache->>API: POST /incoming-payments
    API-->>Apache: Response
    Apache-->>HTTP: Response
    HTTP->>HTTP: Apply ResponseInterceptors (parse JSON)
    HTTP-->>Service: IncomingPayment record
    Service-->>Client: CompletableFuture<IncomingPayment>
    Client-->>User: CompletableFuture<IncomingPayment>
    User->>User: .join() or .thenAccept(...)
```

### Authorization Flow (GNAP)

```mermaid
sequenceDiagram
    participant User as User Code
    participant Client as OpenPaymentsClient
    participant Grant as GrantService
    participant AuthServer as Authorization Server
    participant Browser as User Browser

    User->>Client: client.grants().request(...)
    Client->>Grant: request()
    Grant->>AuthServer: POST to Authorization Server
    AuthServer-->>Grant: Grant with interact URL
    Grant-->>Client: Grant
    Client-->>User: Grant

    alt Grant requires interaction
        User->>Browser: Redirect to grant.getInteractUrl()
        Browser->>AuthServer: User approves
        AuthServer-->>Browser: Redirect with interact_ref
        Browser-->>User: interact_ref
    end

    User->>Client: client.grants().continueGrant(...)
    Client->>Grant: continueGrant()
    Grant->>AuthServer: POST /continue with interact_ref
    AuthServer-->>Grant: AccessToken
    Grant-->>Client: AccessToken
    Client-->>User: AccessToken
```

## Thread Safety & Concurrency

### Thread-Safe Components

- **OpenPaymentsClient**: Fully thread-safe, can be shared across threads
- **All Services**: Stateless, thread-safe
- **HTTP Layer**: Connection pool handles concurrent requests
- **Models**: Immutable, inherently thread-safe

### Virtual Threads Support

Java 25's virtual threads allow efficient blocking:

```java
// Hundreds of concurrent payment operations
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var futures = paymentRequests.stream()
        .map(req -> CompletableFuture.supplyAsync(
            () -> client.incomingPayments().create(req).join(),
            executor
        ))
        .toList();

    var results = futures.stream()
        .map(CompletableFuture::join)
        .toList();
}
```

## Error Handling Architecture

### Exception Hierarchy

```mermaid
classDiagram
    Throwable <|-- RuntimeException
    RuntimeException <|-- OpenPaymentsException
    OpenPaymentsException <|-- AuthenticationException
    OpenPaymentsException <|-- AuthorizationException
    OpenPaymentsException <|-- NotFoundException
    OpenPaymentsException <|-- ValidationException
    OpenPaymentsException <|-- ServerException

    class OpenPaymentsException {
        +base exception
    }
    class AuthenticationException {
        +401, signature failures
    }
    class AuthorizationException {
        +403, insufficient permissions
    }
    class NotFoundException {
        +404, resource not found
    }
    class ValidationException {
        +400, invalid request
    }
    class ServerException {
        +500+, server errors
    }
```

### Error Propagation

1. HTTP errors → Parsed into OpenPaymentsException
2. Exception includes: HTTP status, error code, message
3. CompletableFuture.completeExceptionally() for async errors
4. Users handle via `.exceptionally()` or catch on `.join()`

## Security Architecture

### Authentication Layers

1. **HTTP Signatures**: All requests signed with private key
2. **Access Tokens**: GNAP tokens included as Bearer tokens
3. **TLS**: All communication over HTTPS (enforced)

### Key Management

- Private keys never transmitted
- Public keys published at `/.well-known/jwks.json`
- Signature verification uses public key retrieval

## Performance Considerations

### Connection Pooling

- Apache HttpClient 5 maintains persistent connections
- Default pool: 20 max connections, 5 per route
- Configurable via `OpenPaymentsClientBuilder`

### Async by Default

- Non-blocking I/O prevents thread exhaustion
- CompletableFuture allows composition without blocking
- Virtual threads make blocking on futures cheap

### Caching

- WalletAddress lookups cacheable (optional)
- Public keys cacheable (TTL based)
- Access tokens managed with expiry tracking

## Related Documentation

- **[ADR.md](ADR.md)**: Architecture decisions and rationale
- **[SDK_STRUCTURE.md](SDK_STRUCTURE.md)**: Package organization and file listing
- **[JAVA_25_FEATURES.md](JAVA_25_FEATURES.md)**: Modern Java feature usage
- **[API_COVERAGE.md](API_COVERAGE.md)**: Complete API mapping
- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)**: Code examples and usage patterns
