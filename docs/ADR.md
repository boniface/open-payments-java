# Architecture Decision Records (ADR)

[📚 Back to Documentation Index](INDEX.md) | [🏠 Back to README](../README.md)

---

## Overview

This document records significant architecture and design decisions made during the development of the Open Payments Java SDK. Each decision includes context, the decision itself, consequences, and alternatives considered.

---

## ADR-001: Java 25 as Minimum Version

**Date**: 2025-09-26
**Status**: Accepted
**Deciders**: Development Team

### Context

Open Payments SDK is a new project without legacy constraints. We need to choose a minimum Java version that balances modern features with adoption considerations.

### Decision

Set Java 25 as the minimum supported version.

### Rationale

- **Modern Language Features**: Access to records, sealed classes, pattern matching, and text blocks
- **Virtual Threads**: Built-in support for lightweight concurrency without additional libraries
- **No Legacy Burden**: New SDK allows us to adopt latest features without migration concerns
- **Long-term Viability**: Java's Long LTS support means Java 25 will be current throughout initial development and deployment
- **Target Audience**: Financial/payment SDKs typically target enterprises with modern infrastructure of which Java is a workhorse

### Consequences

**Positive**:
- Significant code reduction using records vs traditional POJOs
- Cleaner async code with virtual threads support
- Better type safety with sealed classes and pattern matching
- Future-proof for upcoming Java features

**Negative**:
- Limited adoption in highly conservative environments still on Java 8/11
- Requires modern build tools (Gradle 9.1+)

**Neutral**:
- Users on older Java versions must upgrade to use this SDK

### Alternatives Considered

1. **Java 17 (LTS)**: More widely adopted but lacks records maturity and sealed classes
2. **Java 21 (LTS)**: Good balance but still 4 versions behind; would miss recent refinements
3. **Java 11 (LTS)**: Maximum compatibility but sacrifices modern features and developer experience

---

## ADR-002: Records for Data Models

**Date**: 2025-09-27
**Status**: Accepted
**Deciders**: Development Team

### Context

SDK requires numerous immutable DTOs for API requests/responses. Traditional approach uses classes with builders, getters, equals/hashCode. Records offer a modern alternative.

### Decision

Use Java records for all immutable data models (Amount, WalletAddress, IncomingPayment, etc.).

### Rationale

- **Immutability by Design**: Records are implicitly final and all fields are final
- **Reduced Boilerplate**: Automatic equals(), hashCode(), toString() implementation
- **Clear Intent**: Record syntax clearly communicates immutability and data-centric nature
- **Compact Constructors**: Built-in validation pattern without constructor bloat
- **Pattern Matching Ready**: Future-proof for when we adopt switch pattern matching

### Consequences

**Positive**:
- Reduced codebase size by a large margin compared to traditional classes
- Compiler-enforced immutability prevents accidental mutation bugs
- Consistent equals/hashCode across all models
- Better debugging with automatic toString()

**Negative**:
- Cannot extend records (no inheritance)
- All fields are public (breaks encapsulation purists' expectations)
- Requires Java 14+ (mitigated by ADR-001)

**Neutral**:
- Builders still needed for complex objects, but implemented as nested static classes

### Alternatives Considered

1. **Lombok @Value**: External dependency, generates code at compile-time, less explicit
2. **Traditional Classes**: More flexible but 3x more code, error-prone equals/hashCode
3. **Kotlin Data Classes**: Would require polyglot project, unacceptable for Java SDK

---

## ADR-003: CompletableFuture for Async Operations

**Date**: 2025-09-27
**Status**: Accepted
**Deciders**: Development Team

### Decision

All service methods return `CompletableFuture<T>` instead of blocking operations.

### Rationale

- **Non-blocking by Default**: Payment operations involve network I/O; blocking threads wastes resources
- **Standard Library**: CompletableFuture is part of Java, no external dependency required
- **Composability**: Supports chaining, combining, and transforming async operations
- **Virtual Threads Compatible**: Works seamlessly with Java 25 virtual threads for efficient blocking
- **Familiar API**: Most Java developers understand CompletableFuture patterns

### Consequences

**Positive**:
- Better resource utilization with non-blocking I/O
- Supports both sync (`.join()`) and async usage patterns
- Natural fit for reactive/async application architectures
- Easy to compose complex workflows (e.g., quote → payment → verification)

**Negative**:
- Learning curve for developers unfamiliar with async programming
- More complex error handling compared to exceptions
- Potential for callback hell if not used carefully

**Neutral**:
- Users wanting synchronous behavior can simply call `.join()`

### Alternatives Considered

1. **Reactive Streams (Project Reactor/RxJava)**: Powerful but additional dependency, steeper learning curve
2. **Synchronous Only**: Simpler but wastes threads on I/O, poor scalability
3. **Callback-based**: Flexible but leads to callback hell, no standard library support
4. **Kotlin Coroutines**: Excellent async model but requires Kotlin dependency

---

## ADR-004: Interface-based Service Layer

**Date**: 2025-09-28
**Status**: Accepted
**Deciders**: Development Team

### Context

SDK needs to expose payment operations (wallet, incoming/outgoing payments, quotes, grants). We must decide between concrete classes or interfaces.

### Decision

Define all services as interfaces (WalletAddressService, IncomingPaymentService, etc.) with implementation classes separate.

### Rationale

- **Testability**: Users can easily mock services in unit tests
- **Dependency Injection**: Interfaces enable DI frameworks (Spring, Guice, etc.)
- **Multiple Implementations**: Allows future HTTP client alternatives (OkHttp, native HTTP client, etc.)
- **Clear Contracts**: Interface defines public API separate from implementation details
- **Evolution**: Can add default methods without breaking existing implementations

### Consequences

**Positive**:
- Excellent testability with mock implementations
- Clean separation of API contract from implementation
- Easy to create test doubles or alternative implementations
- DI-friendly for enterprise applications

**Negative**:
- Slightly more files (interface + implementation)
- Requires factory or builder to create concrete implementations
- Cannot evolve interface easily (breaking change to add methods)

**Neutral**:
- Default methods in interfaces mitigate evolution challenges (Java 8+)

### Alternatives Considered

1. **Abstract Classes**: More flexibility but prevents multiple inheritance, harder to mock
2. **Concrete Classes Only**: Simpler but poor testability, tight coupling
3. **Sealed Interfaces**: Overly restrictive for a public SDK

---

## ADR-005: Apache HttpClient 5 for HTTP Layer

**Date**: 2025-09-28
**Status**: Accepted
**Deciders**: Development Team

### Context

SDK requires solid HTTP client for REST API communication. Must support connection pooling, timeouts, interceptors, and authentication.

### Decision

Use Apache HttpClient 5 as the underlying HTTP implementation.

### Rationale

- **Mature & Proven**: Industry-standard HTTP client used in countless production systems
- **Virtual Thread Support**: Version 5.x supports Java 19+ virtual threads for efficient concurrency
- **Connection Pooling**: Built-in connection pool management for performance
- **Interceptors**: Request/response interceptor mechanism for authentication and logging
- **Standards Compliance**: Full HTTP/1.1 and HTTP/2 support
- **Active Maintenance**: Apache actively maintains and updates the library

### Consequences

**Positive**:
- Production-grade reliability and performance
- Extensive documentation and community support
- Built-in retry, authentication, and timeout mechanisms
- Works seamlessly with HTTP signatures library (Tomitribe)

**Negative**:
- Additional dependency (~500KB)
- More complex than simple HTTP clients
- Learning curve for advanced features

**Neutral**:
- Wrapped behind HttpClient interface, can swap if needed

### Alternatives Considered

1. **Java Native HTTP Client**: Lightweight, no dependency, but less mature and fewer features
2. **OkHttp**: Popular in Android, but oriented toward mobile use cases
3. **Netty**: Excellent for async I/O but overkill for REST API client
4. **Apache HttpComponents 4.x**: Older version without virtual threads support

---

## ADR-006: Builder Pattern for Complex Objects

**Date**: 2025-09-29
**Status**: Accepted
**Deciders**: Development Team

### Context

Some objects (client configuration, payment requests) have many optional parameters. Constructors with many parameters are error-prone and hard to read.

### Decision

Implement builder pattern for objects with 3+ fields or any optional fields.

### Rationale

- **Readability**: Named methods clearly indicate what's being set
- **Optional Parameters**: Clean handling of optional fields without multiple constructors
- **Immutability**: Builders produce immutable objects
- **Validation**: Centralized validation in `build()` method
- **Fluent API**: Chainable method calls improve code aesthetics

### Consequences

**Positive**:
- Highly readable client configuration and request building
- Easy to add new optional parameters without breaking existing code
- Type-safe with compile-time checking
- Natural fit with functional programming (Consumer<Builder> pattern)

**Negative**:
- More code (builder class for each buildable object)
- Slight runtime overhead creating builder instance
- Can be overused for simple objects

**Neutral**:
- Records can have nested static Builder classes, maintaining immutability

### Alternatives Considered

1. **Telescoping Constructors**: Multiple overloaded constructors, gets unwieldy quickly
2. **JavaBeans Pattern**: Setters on mutable objects, violates immutability principle
3. **Named Parameters**: Not available in Java (Kotlin feature)
4. **Lombok @Builder**: External dependency, less explicit

---

## ADR-007: GNAP for Authorization

**Date**: 2025-09-29
**Status**: Accepted (Specification Requirement)
**Deciders**: Open Payments Specification

### Context

Open Payments specification mandates GNAP (Grant Negotiation and Authorization Protocol) for authorization flows.

### Decision

Implement GrantService following GNAP protocol as specified in Open Payments.

### Rationale

- **Specification Compliance**: Required by Open Payments, not negotiable
- **Modern OAuth Alternative**: More flexible than OAuth 2.0 for complex authorization scenarios
- **Support for Interactive Flows**: Handles user consent flows naturally
- **Token Management**: Built-in token rotation and revocation
- **Fine-grained Permissions**: Access rights model allows granular control

### Consequences

**Positive**:
- Full compliance with Open Payments specification
- Modern authorization pattern better than OAuth 2.0 for payment scenarios
- Built-in security best practices (token rotation, short-lived tokens)

**Negative**:
- GNAP less familiar than OAuth 2.0 to most developers
- More complex than simple API key authentication
- Requires interactive user flow for some operations

**Neutral**:
- SDK abstracts GNAP complexity behind clean GrantService API

### Alternatives Considered

None - this is a specification requirement, not a choice.

---

## ADR-008: HTTP Signatures for Authentication

**Date**: 2025-09-30
**Status**: Accepted (Specification Requirement) - ✅ Implemented
**Deciders**: Open Payments Specification

### Context

Open Payments requires HTTP message signatures for authenticating requests to resource servers.

### Decision

Implement HTTP Signatures (RFC 9421) with Ed25519 signing using custom implementation.

### Rationale

- **Specification Compliance**: Open Payments mandates HTTP signatures (RFC 9421)
- **Custom Implementation**: Built HttpSignatureService with Ed25519 support
- **Sign Request Components**: Signs method, URI, content-digest headers
- **Public Key Verification**: Recipients verify signatures using published JWKs
- **Tamper Protection**: Prevents request modification in transit
- **Zero External Dependencies**: No third-party signature libraries needed

### Consequences

**Positive**:
- Strong authentication without sending credentials in requests
- Tamper-proof requests provide message integrity
- Standards-based approach (RFC 9421)
- Works seamlessly with GNAP access tokens
- No external dependencies for signature generation
- Full control over signature component selection

**Negative**:
- Cryptographic key management required (Ed25519 key pairs)
- Clock skew can cause signature validation failures
- Must maintain signature implementation as RFC evolves

**Neutral**:
- Wrapped in HttpSignatureService, transparent to service layer
- ContentDigest utility handles SHA-256 body digests

### Implementation Details

**Implemented in Phase 1 & 2** (102 tests):

**Phase 1 - Cryptography**:
- `ClientKey` - Ed25519 key pair representation
- `ClientKeyGenerator` - Key generation using Java's KeyPairGenerator ("Ed25519")
- `ContentDigest` - SHA-256 digest calculation
- `Base64Encoder` - URL-safe Base64 encoding
- Uses Java 15+ built-in Ed25519 support (no external crypto libraries)

**Phase 2 - HTTP Signatures**:
- `HttpSignatureService` - Main service for request signing
- `SignatureComponents` - Component selection and serialization
- `SignatureInput` - Signature metadata generation
- RFC 9421 compliant signature generation

### Alternatives Considered

Library choice alternatives:

1. **Custom Implementation (chosen)**: Full control, no dependencies, leverages Java's built-in Ed25519
2. **Tomitribe HTTP Signatures**: External dependency, would still need Ed25519 implementation
3. **Bouncy Castle**: Heavy crypto library (1MB+), unnecessary when Java 15+ has Ed25519 built-in

---

## ADR-009: Jackson for JSON Serialization

**Date**: 2025-09-30
**Status**: Accepted
**Deciders**: Development Team

### Context

SDK needs robust JSON serialization/deserialization for REST API communication. Must handle Java Time types, null values, and unknown properties gracefully.

### Decision

Use Jackson for all JSON operations with centralized ObjectMapper configuration.

### Rationale

- **Industry Standard**: De facto JSON library for Java, used by Spring and most frameworks
- **Feature Rich**: Handles complex scenarios (polymorphism, generics, dates)
- **Java Time Support**: Module available for modern java.time.* types
- **Annotation-based**: Clean model annotations for field mapping
- **Performance**: Faster than alternatives (Gson, JSON-B)
- **Active Development**: Regularly updated, excellent community support

### Consequences

**Positive**:
- Reliable serialization of all SDK models
- Excellent support for Java Time types (Instant, Duration, etc.)
- Can ignore unknown properties for forward compatibility
- Wide ecosystem integration (testing tools, frameworks, etc.)

**Negative**:
- Additional dependency (~500KB with modules)
- Complex configuration for edge cases
- Requires jackson-datatype-jsr310 module for Java Time

**Neutral**:
- JsonMapper utility class centralizes configuration, hiding Jackson from most code

### Alternatives Considered

1. **Gson**: Simpler but slower, lacks some features, less active development
2. **JSON-B**: Java EE standard but less flexible, smaller ecosystem
3. **org.json**: Too basic for production use, no annotation support
4. **Manual Parsing**: Error-prone, massive development effort

---

## ADR-010: Checkstyle + Spotless for Code Quality

**Date**: 2025-10-01
**Status**: Accepted
**Deciders**: Development Team

### Context

Project needs consistent code formatting and style enforcement across all contributors. Manual formatting is error-prone and creates noisy diffs.

### Decision

Use Spotless (Eclipse JDT formatter) for automatic formatting and Checkstyle for validation, integrated into the build process.

### Rationale

- **Automatic Formatting**: Spotless applies formatting automatically on compile
- **Zero Configuration**: Eclipse JDT formatter works with all Java versions
- **Build Integration**: Enforced via Gradle, prevents unformatted code from being committed
- **Consistent Style**: All code follows same style regardless of IDE or developer
- **Fast Feedback**: Violations caught immediately during `./gradlew build`

### Consequences

**Positive**:
- Zero style debates or bikeshedding
- Clean Git diffs (no formatting-only changes)
- Consistent codebase appearance
- IDE-agnostic (works same in IntelliJ, Eclipse, VS Code)

**Negative**:
- Slightly slower build (formatting step added)
- Occasionally formats code in unexpected ways
- Custom Checkstyle rules can be restrictive

**Neutral**:
- Developers can run `./gradlew spotlessApply` manually to format before commit

### Alternatives Considered

1. **Google Java Format**: Java 25 compatibility issues (NoSuchMethodError)
2. **Palantir Java Format**: Not available in Maven Central
3. **IntelliJ Formatter**: IDE-specific, not portable
4. **Manual Enforcement**: Code review burden, inconsistent results

---

## ADR-011: Apache 2.0 License

**Date**: 2025-10-01
**Status**: Accepted
**Deciders**: Development Team

### Context

Open source SDK requires a permissive license that encourages adoption while providing patent protection.

### Decision

Release under Apache License 2.0.

### Rationale

- **Most Permissive**: Allows commercial use, modification, and distribution
- **Patent Grant**: Explicit patent license protects users from patent claims
- **Industry Standard**: Widely understood and accepted by enterprises
- **License Compatibility**: Compatible with most other open source licenses
- **No Copyleft**: Users can incorporate into proprietary software
- **Attribution Only**: Only requirement is to preserve copyright notices

### Consequences

**Positive**:
- Maximum adoption potential (commercial and open source)
- Patent protection for all users
- Enterprise-friendly (legal teams understand Apache 2.0)
- Can be included in both open and closed source projects

**Negative**:
- No guarantee derivatives remain open source (unlike GPL)
- Companies can create proprietary forks without contributing back

**Neutral**:
- Well-understood license terms, minimal legal friction

### Alternatives Considered

1. **MIT**: Simpler but no explicit patent grant
2. **GPL**: Copyleft would reduce adoption in commercial settings
3. **BSD**: Similar to MIT, lacks patent protection
4. **Unlicense/Public Domain**: Too permissive, no contributor protection

---

## ADR-012: No Reactive Streams Dependency

**Date**: 2025-10-01
**Status**: Accepted
**Deciders**: Development Team

### Context

Async operations could use Reactive Streams (Reactor/RxJava) for more powerful composition. Must balance power vs complexity.

### Decision

Use CompletableFuture (stdlib) instead of Reactive Streams libraries.

### Rationale

- **Zero Dependencies**: CompletableFuture is in Java standard library
- **Good Enough**: SDK operations are simple request/response, not complex streams
- **Lower Barrier**: Most Java developers understand CompletableFuture
- **Lighter Weight**: No 5MB+ reactive library dependency
- **Virtual Threads**: Java 25 virtual threads provide efficient blocking on CompletableFuture

### Consequences

**Positive**:
- No external dependencies for async operations
- Simpler mental model for SDK users
- Smaller dependency footprint
- Easy to wrap in Reactor/RxJava if users need it

**Negative**:
- Less powerful composition than Reactor (no backpressure, operators)
- No built-in retry/circuit breaker patterns
- Can't directly integrate with reactive frameworks without adapters

**Neutral**:
- Users can wrap CompletableFuture in reactive types if needed

### Alternatives Considered

1. **Project Reactor**: Powerful but 5MB+ dependency, steep learning curve
2. **RxJava**: Similar to Reactor, more Android-oriented
3. **Kotlin Coroutines**: Excellent model but requires Kotlin dependency
4. **Flow API**: Java 9+ but more complex than needed for this SDK

---

## ADR-013: Service-Oriented Package Structure

**Date**: 2025-10-02
**Status**: Accepted
**Deciders**: Development Team

### Context

SDK covers multiple API domains (wallet, payments, quotes, auth). Package organization must be intuitive and scalable.

### Decision

Organize code by service domain: `wallet/`, `payment/{incoming,outgoing,quote}/`, `auth/`, with each containing service interface and related models.

### Rationale

- **Domain-Driven**: Packages reflect Open Payments API structure
- **Cohesion**: Related models and services grouped together
- **Discoverability**: Developers find code where they expect it
- **Scalability**: Easy to add new services without restructuring
- **Separation**: Clear boundaries between payment types (incoming vs outgoing)

### Consequences

**Positive**:
- Intuitive navigation (payment/incoming for incoming payment code)
- Models close to services that use them
- Mirrors Open Payments documentation structure
- Easy to understand for newcomers

**Negative**:
- Some duplication (each service has Request model)
- Deeper package nesting than flat structure
- Common models split between packages and `model/`

**Neutral**:
- Common models (Amount, PaginatedResult) live in top-level `model/` package

### Alternatives Considered

1. **Layered (service/, model/, dto/)**: Separates by technical concern, less intuitive
2. **Flat (all in openpayments/)**: Simple but doesn't scale, hard to navigate
3. **Feature-based (p2p/, ecommerce/)**: Doesn't map to API structure, confusing

---

## ADR-014: HTTP Interceptor Pattern

**Date**: 2025-10-10
**Status**: Accepted - ✅ Implemented
**Deciders**: Development Team

### Context

HTTP communication requires cross-cutting concerns: logging, authentication, error handling. These shouldn't be scattered throughout service implementations.

### Decision

Implement functional interceptor pattern with RequestInterceptor and ResponseInterceptor interfaces that can be chained on HTTP clients.

### Rationale

- **Separation of Concerns**: Authentication, logging, errors handled independently
- **Composability**: Multiple interceptors can be chained in sequence
- **Reusability**: Same interceptor can be used across all services
- **Transparency**: Service layer doesn't know about interceptors
- **Functional Interface**: Single abstract method enables lambda-based interceptors
- **Immutability**: Interceptors return new request/response, don't mutate

### Consequences

**Positive**:
- Clean separation: services focus on business logic, interceptors handle infrastructure
- Easy to add new concerns (rate limiting, metrics, caching) without touching services
- Testability: interceptors tested independently, services tested with mock interceptors
- Security: sensitive header masking in logging interceptor
- Flexible ordering: interceptors execute in the order added

**Negative**:
- Slight performance overhead (each interceptor creates new object)
- Order matters: incorrect ordering can cause issues (e.g., logging before auth)
- Debugging: multiple interceptors can make request flow harder to trace

**Neutral**:
- Each HTTP client instance has its own interceptor chain

### Implementation Details

**Implemented in Phase 5** (79 tests):

1. **Request Interceptors**:
   - `LoggingRequestInterceptor` - Logs requests with sensitive header masking
   - `AuthenticationInterceptor` - Adds authentication headers (Bearer, GNAP, Basic, Custom)

2. **Response Interceptors**:
   - `LoggingResponseInterceptor` - Logs responses with configurable log levels
   - `ErrorHandlingInterceptor` - Extracts structured error information from JSON

3. **Features**:
   - Thread-safe with ConcurrentHashMap for sensitive patterns
   - Configurable log levels and verbosity
   - Automatic sensitive data masking (Authorization, tokens, keys)
   - Large body truncation for performance
   - JSON error parsing with fallback

### Alternatives Considered

1. **Filter Chain Pattern**: More complex, requires managing filter chain state
2. **Aspect-Oriented Programming (AOP)**: Would require Spring/AspectJ dependency
3. **Decorator Pattern**: More rigid, harder to compose dynamically
4. **Inline Logic**: Would scatter concerns across all service implementations

---

## ADR-015: Immutability Throughout

**Date**: 2025-10-02
**Status**: Accepted
**Deciders**: Development Team

### Context

Payment data is sensitive. Mutable objects can lead to subtle bugs where data changes unexpectedly.

### Decision

Make all data models immutable: records with final fields, defensive copying for collections.

### Rationale

- **Thread Safety**: Immutable objects are inherently thread-safe
- **Predictability**: Object state cannot change after construction
- **Cacheable**: Safe to cache immutable objects without worrying about mutations
- **Debugging**: Easier to reason about code when data doesn't change
- **Security**: Prevents accidental or malicious modification of payment data

### Consequences

**Positive**:
- Thread-safe by default, no synchronization needed
- Safer handling of payment-sensitive data
- Eliminates entire class of bugs (unexpected mutations)
- Can safely share objects between threads

**Negative**:
- Must create new object for any change (can't modify in-place)
- Slightly more memory usage (new objects vs mutation)
- Builders required for complex object creation

**Neutral**:
- Defensive copying in constructors adds minor overhead but ensures safety

### Alternatives Considered

1. **Mutable JavaBeans**: Traditional but thread-unsafe, error-prone
2. **Unmodifiable Wrappers**: Half-measure, original object can still change
3. **Immutable.js Pattern**: Popular in JavaScript, not idiomatic Java

---

## Summary

These ADRs capture the key architecture decisions shaping the Open Payments Java SDK:

### Core Principles (Phases 1-6 Complete)

- **Modern Java** (ADR-001): Java 25 features for cleaner, safer code
- **Immutability** (ADR-002, ADR-015): Records and immutable data models throughout
- **Async-First** (ADR-003): Non-blocking operations with CompletableFuture
- **Type Safety**: Records, interfaces, and strong typing throughout
- **Testability** (ADR-004): Interface-based design for easy mocking

### Implementation Choices (✅ Implemented)

- **HTTP Signatures** (ADR-008): Custom RFC 9421 implementation with Ed25519
- **GNAP Authorization** (ADR-007): Full GNAP protocol for grant management
- **HTTP Interceptors** (ADR-014): Functional interceptor pattern for cross-cutting concerns
- **Apache HttpClient 5** (ADR-005): Production-grade HTTP with virtual thread support
- **Jackson JSON** (ADR-009): Industry-standard serialization with Java Time support

### Quality & Standards

- **Code Quality** (ADR-010): Spotless + Checkstyle for consistent formatting
- **No Reactive Streams** (ADR-012): CompletableFuture over heavy dependencies
- **Service-Oriented Structure** (ADR-013): Domain-driven package organization
- **Apache 2.0 License** (ADR-011): Maximum adoption with patent protection

### Status Summary

**Total ADRs**: 15
**Implemented**: 6 (ADR-001 through ADR-006 form foundation, ADR-007/08/14 implemented in Phases 1-6)
**Accepted**: 9 (ADR-009 through ADR-015, with ADR-014 completed)

All architectural decisions through Phase 6 are complete and validated with 277 passing tests.


