# Open Payments Java SDK - Structure Overview

[📚 Back to Documentation Index](INDEX.md) | [🏠 Back to README](../README.md)

---

## Purpose

This document provides a detailed breakdown of the package structure and file organization. For architecture decisions, see [ADR.md](ADR.md). For runtime architecture, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Package Structure

```
zm.hashcode.openpayments/
├── client/                       # Main client API
├── auth/                         # Authentication & Authorization (GNAP)
│   ├── exception/                # Auth-specific exceptions
│   ├── grant/                    # GNAP grant protocol
│   ├── keys/                     # Client key management
│   ├── signature/                # HTTP message signatures
│   └── token/                    # Token management
├── wallet/                       # Wallet Address operations
├── payment/                      # Payment operations
│   ├── incoming/                 # Incoming payments
│   ├── outgoing/                 # Outgoing payments
│   └── quote/                    # Payment quotes
├── http/                         # HTTP abstraction layer
│   ├── config/                   # HTTP client configuration
│   ├── core/                     # Core HTTP interfaces
│   ├── factory/                  # HTTP client factory
│   ├── impl/                     # HTTP client implementations
│   ├── interceptor/              # Request/response interceptors
│   └── resilience/               # Retry and resilience
├── util/                         # Cross-cutting utilities
└── model/                        # Shared models and exceptions
```

## Core Components (23 files)

### Client API (2 files)
- `OpenPaymentsClient` - Main SDK entry point interface
- `OpenPaymentsClientBuilder` - Fluent builder for client configuration

### Authentication & Authorization (5 files)
- `GrantService` - GNAP grant management interface
- `Grant` - Grant resource model
- `GrantRequest` - Grant request builder
- `AccessToken` - Access token model with expiry tracking
- `AccessRight` - Access rights and permissions model

### Wallet Operations (4 files)
- `WalletAddressService` - Wallet address operations interface
- `WalletAddress` - Wallet address resource model
- `PublicKey` - Public key model
- `PublicKeySet` - Set of public keys

### Payment Operations (9 files)

**Incoming Payments:**
- `IncomingPaymentService` - Interface for receiving payments
- `IncomingPayment` - Incoming payment resource
- `IncomingPaymentRequest` - Request builder

**Outgoing Payments:**
- `OutgoingPaymentService` - Interface for sending payments
- `OutgoingPayment` - Outgoing payment resource
- `OutgoingPaymentRequest` - Request builder

**Quotes:**
- `QuoteService` - Interface for payment quotes
- `Quote` - Quote resource
- `QuoteRequest` - Quote request builder

### Common Models (3 files)
- `Amount` - Money representation with asset code and scale
- `PaginatedResult<T>` - Generic pagination wrapper
- `OpenPaymentsException` - Base exception for SDK errors

## Package Dependency Graph

```
model/          (no dependencies - pure data)
   ↑
   │
util/           (depends on: model)
   ↑
   │
http/           (depends on: model, util)
   ↑
   │
auth/           (depends on: model, http, util)
wallet/         (depends on: model, http, util)
payment/*       (depends on: model, http, util)
   ↑
   │
client/         (depends on: all packages)
```

### Dependency Rules
1. **No Circular Dependencies**: Enforced through package structure
2. **Model is Pure**: Contains only data, no business logic
3. **Util is Shared**: Used by all layers
4. **Services Depend on HTTP**: All network calls go through http layer
5. **Client Coordinates**: Top-level package ties everything together

## Detailed Package Contents

### client/ (2 files)
**Purpose**: Main SDK entry point and configuration

| File | Type | Purpose |
|------|------|---------|
| `OpenPaymentsClient` | Interface | Main client API, returns service instances |
| `OpenPaymentsClientBuilder` | Interface | Fluent builder for client configuration |

**Key Methods**:
- `OpenPaymentsClient.builder()` - Factory method
- `walletAddresses()` / `incomingPayments()` / etc. - Service accessors
- `close()` - Resource cleanup

---

### auth/ (5 files)
**Purpose**: GNAP authorization protocol implementation

| File | Type | Purpose |
|------|------|---------|
| `GrantService` | Interface | Grant lifecycle operations |
| `Grant` | Record | Grant resource with tokens |
| `GrantRequest` | Class | Builder for grant requests |
| `AccessToken` | Record | Access token with expiry |
| `AccessRight` | Record | Permission descriptor |

**Key Operations**:
- Request grant with access rights
- Continue grant after user interaction
- Rotate tokens
- Revoke tokens

---

### wallet/ (4 files)
**Purpose**: Wallet address discovery and public key retrieval

| File | Type | Purpose |
|------|------|---------|
| `WalletAddressService` | Interface | Wallet address operations |
| `WalletAddress` | Record | Wallet metadata |
| `PublicKey` | Record | Single public key (JWK) |
| `PublicKeySet` | Record | Collection of public keys |

**Key Operations**:
- GET wallet address metadata
- GET public keys for signature verification

---

### payment/incoming/ (3 files)
**Purpose**: Receiving payments

| File | Type | Purpose |
|------|------|---------|
| `IncomingPaymentService` | Interface | Incoming payment operations |
| `IncomingPayment` | Record | Incoming payment resource |
| `IncomingPaymentRequest` | Class | Builder for payment creation |

**Key Operations**:
- Create incoming payment
- Get payment by ID
- List payments with pagination
- Complete payment

---

### payment/outgoing/ (3 files)
**Purpose**: Sending payments

| File | Type | Purpose |
|------|------|---------|
| `OutgoingPaymentService` | Interface | Outgoing payment operations |
| `OutgoingPayment` | Record | Outgoing payment resource |
| `OutgoingPaymentRequest` | Class | Builder for payment creation |

**Key Operations**:
- Create outgoing payment
- Get payment by ID
- List payments with pagination

---

### payment/quote/ (3 files)
**Purpose**: Payment quotes (exchange rates, fees)

| File | Type | Purpose |
|------|------|---------|
| `QuoteService` | Interface | Quote operations |
| `Quote` | Record | Quote resource |
| `QuoteRequest` | Class | Builder for quote creation |

**Key Operations**:
- Create quote (sender or receiver amounts)
- Get quote by ID

---

### model/ (3 files)
**Purpose**: Shared data models

| File | Type | Purpose |
|------|------|---------|
| `Amount` | Record | Money representation |
| `PaginatedResult<T>` | Record | Generic pagination wrapper |
| `OpenPaymentsException` | Class | Base exception |

**Used By**: All packages

---

### http/ (6 files)
**Purpose**: HTTP abstraction layer

| File | Type | Purpose |
|------|------|---------|
| `HttpClient` | Interface | HTTP operations contract |
| `HttpRequest` | Record | Immutable HTTP request |
| `HttpResponse` | Record | Immutable HTTP response |
| `HttpMethod` | Enum | GET, POST, PATCH, DELETE |
| `RequestInterceptor` | FunctionalInterface | Request modification |
| `ResponseInterceptor` | FunctionalInterface | Response processing |

**Implementation**: Apache HttpClient 5 (not in this package)

---

### util/ (3 files)
**Purpose**: Cross-cutting utilities

| File | Type | Purpose |
|------|------|---------|
| `JsonMapper` | Final Class | Jackson wrapper for JSON |
| `UrlBuilder` | Final Class | Fluent URL construction |
| `Validators` | Final Class | Input validation helpers |

**Used By**: HTTP layer, services, builders

---

## File Statistics

| Metric | Count |
|--------|-------|
| **Total Java Files** | 32 |
| **Interfaces** | 7 (client + 6 services) |
| **Records** | 12 (immutable data models) |
| **Classes** | 10 (builders, utils, exceptions) |
| **Enums** | 1 (HttpMethod) |
| **Functional Interfaces** | 2 (interceptors) |

## Naming Conventions

### Packages
- All lowercase: `zm.hashcode.openpayments.payment.incoming`
- Plural for collections: `payment**s**`, not `payment`

### Classes/Interfaces
- PascalCase: `IncomingPaymentService`
- Services end with `Service`
- Requests end with `Request`
- Builders are nested: `ClassName.Builder`

### Methods
- camelCase: `createIncomingPayment()`
- Async methods return `CompletableFuture<T>`
- Builder methods return `this` for chaining

### Records
- PascalCase: `Amount`, `WalletAddress`
- Components are lowercase: `value`, `assetCode`
- Helper methods use `get` prefix for Optional: `getPublicName()`

## Build Commands

```bash
# Format all code
./gradlew spotlessApply

# Compile (triggers formatting)
./gradlew compileJava

# Full build with checks
./gradlew build

# Checkstyle validation
./gradlew checkstyleMain
```

## Related Documentation

- **[ADR.md](ADR.md)**: Why these structure decisions were made
- **[ARCHITECTURE.md](ARCHITECTURE.md)**: Runtime architecture and component interactions
- **[JAVA_25_FEATURES.md](JAVA_25_FEATURES.md)**: Modern Java features used
- **[API_COVERAGE.md](API_COVERAGE.md)**: Mapping to Open Payments API
