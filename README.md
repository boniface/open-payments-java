# Open Payments Java SDK

[![Project Stage](https://img.shields.io/badge/Project%20Stage-Development-yellow.svg)]()
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![CI](https://img.shields.io/badge/CI-passing-brightgreen.svg)]()
[![Test Coverage](https://img.shields.io/badge/Coverage-0%25-red.svg)]()
[![Release](https://img.shields.io/badge/Release-0.1.0--SNAPSHOT-blue.svg)]()
[![Snapshot](https://img.shields.io/badge/Snapshot-available-blue.svg)]()
[![JavaDoc](https://img.shields.io/badge/JavaDoc-latest-blue.svg)]()
[![Issues](https://img.shields.io/github/issues/boniface/open-payments-java.svg)](https://github.com/boniface/open-payments-java/issues)

---

## 📋 Quick Navigation

**[Overview](#overview)** | **[ Project Status](#project-status)** | **[ License](#license)** | **[ Contributing](#contributing)** | **[ Documentation](docs/)**

---

<a name="overview"></a>
## 📖 Overview

### What is Open Payments?

**[Open Payments](https://openpayments.dev)** is an open, RESTful API standard that enables applications to interact with financial accounts across different providers (banks, digital wallets, mobile money providers) in a standardized way. It allows developers to add payment functionality to their applications **without becoming licensed financial operators** or building custom integrations for each financial institution.

### The Open Payments Ecosystem

The Open Payments ecosystem consists of:

- **Account Servicing Entities (ASEs)**: Banks, digital wallets, or payment providers that implement the Open Payments API to expose their accounts
- **Resource Servers**: ASE-operated servers that handle payment operations (incoming payments, outgoing payments, quotes)
- **Authorization Servers**: Servers that manage access control using the GNAP (Grant Negotiation and Authorization Protocol)
- **Client Applications**: Your applications that use this SDK to interact with Open Payments-enabled accounts

Open Payments **separates payment instructions from actual fund execution** - the API lets you create payment requests and authorize transactions, while the ASE handles the actual movement of money.

### How It Works

![](/docs/payment_arch.png)


**This SDK handles**: Communication layer (top box)
**Open Payments API handles**: Protocol and authorization (middle box)
**ASE handles**: Actual fund movement (bottom box)

## What Does This SDK Do?

This **Java SDK** is a client library that simplifies interaction with the Open Payments API from Java applications. Instead of manually constructing HTTP requests, handling authentication, and parsing responses, this SDK provides:

-  **Type-safe Java methods** for all Open Payments API operations
-  **Automatic authentication** using HTTP signatures and GNAP tokens
-  **Request/response serialization** with proper JSON mapping
-  **Error handling** with meaningful exceptions
-  **Modern Java patterns** (records, CompletableFuture, builders)

**Important**: This SDK is a **client library**, not a payment processor. It enables your Java application to:
- Communicate with Open Payments-enabled accounts (ASEs)
- Request authorizations for payment operations
- Create, retrieve, and manage payments, quotes, and grants
- Handle the Open Payments protocol flow (discovery, authorization, execution)

**The actual payment processing** is performed by the Account Servicing Entities (banks, wallets) that implement the Open Payments specification.

## Use Cases Enabled by Open Payments

When your application uses this SDK to interact with Open Payments APIs, you can build:

-  **Peer-to-Peer Payments** - Enable users to send money between Open Payments-enabled accounts
-  **E-commerce Checkout** - Accept payments from any Open Payments wallet
-  **Recurring Payments** - Set up subscriptions with Open Payments authorization
-  **Web Monetization** - Receive micropayments for content consumption
-  **Invoicing** - Create payment requests with expiration dates
-  **Buy Now, Pay Later** - Defer payment execution to a future date
-  **Split Payments** - Distribute payments across multiple recipients

**Note**: These features work by making API calls to Open Payments-compliant servers. Your application acts as a client; the payment infrastructure is provided by the ASEs.

## SDK Features

### Technical Capabilities
-  **Modern Java 25** - Leverages records, var, and latest language features
-  **Type-Safe** - Strongly typed with compile-time safety
-  **Async-First** - All operations return `CompletableFuture<T>`
-  **Immutable** - All data models are immutable records
-  **Fluent API** - Builder pattern for easy configuration
-  **Thread-Safe** - Safe for concurrent use
-  **Well-Documented** - Comprehensive JavaDoc and guides

### Complete API Coverage
-  **Wallet Address Discovery** - Retrieve account metadata and public keys
-  **Incoming Payments** - Create and manage payment requests
-  **Outgoing Payments** - Initiate payments from authorized accounts
-  **Quotes** - Get exchange rates and fee information
-  **GNAP Authorization** - Handle grant requests and token management
-  **HTTP Signatures** - Automatic request signing for authentication

## Quick Start

### Prerequisites

- Java 25 or later
- Gradle 9.1 or later (wrapper included)

### Installation

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("zm.hashcode:open-payments-java:1.0-SNAPSHOT")
}
```

Or with Maven:

```xml
<dependency>
    <groupId>zm.hashcode</groupId>
    <artifactId>open-payments-java</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Basic Usage

```java
import zm.hashcode.openpayments.client.OpenPaymentsClient;
import zm.hashcode.openpayments.model.Amount;

// Initialize client
var client = OpenPaymentsClient.builder()
    .walletAddress("https://wallet.example.com/alice")
    .privateKey(privateKey)
    .keyId(keyId)
    .build();

// Get wallet information
var wallet = client.walletAddresses()
    .get("https://wallet.example.com/alice")
    .join();

System.out.println("Wallet: " + wallet.id());
System.out.println("Asset: " + wallet.assetCode());

// Create incoming payment
var payment = client.incomingPayments()
    .create(request -> request
        .walletAddress(wallet.id())
        .incomingAmount(Amount.of("1000", "USD", 2))
        .expiresAt(Instant.now().plus(Duration.ofDays(1))))
    .join();

System.out.println("Payment URL: " + payment.getId());

// Close client when done
client.close();
```

## Use Cases & Examples

### 💸 Peer-to-Peer Payment

```java
// Alice sends $50 to Bob
var quote = client.quotes()
    .create(q -> q
        .walletAddress(aliceWallet)
        .receiver(bobIncomingPayment)
        .sendAmount(Amount.of("5000", "USD", 2)))
    .join();

var payment = client.outgoingPayments()
    .create(p -> p.walletAddress(aliceWallet).quoteId(quote.id()))
    .join();
```

### 🛒 E-commerce Checkout

```java
// Create payment request for customer checkout
var checkoutPayment = client.incomingPayments()
    .create(request -> request
        .walletAddress(merchantWallet)
        .incomingAmount(Amount.of("9999", "USD", 2))
        .expiresAt(Instant.now().plus(Duration.ofHours(1)))
        .metadata("{\"orderId\": \"ORD-12345\", \"items\": 3}"))
    .join();

// Share payment URL with customer
System.out.println("Pay here: " + checkoutPayment.getId());
```

### 🔄 Recurring Payments (Subscriptions)

```java
// Monthly subscription payment
var subscription = client.incomingPayments()
    .create(request -> request
        .walletAddress(serviceWallet)
        .incomingAmount(Amount.of("999", "USD", 2)) // $9.99/month
        .metadata("{\"subscription\": \"premium\", \"period\": \"monthly\"}"))
    .join();

// Application logic handles recurring authorization and payment
```

### 🌐 Web Monetization

```java
// Micropayments for content consumption
var monetization = client.incomingPayments()
    .create(request -> request
        .walletAddress(contentCreatorWallet)
        .metadata("{\"contentId\": \"article-123\", \"type\": \"web-monetization\"}"))
    .join();

// Stream small payments as content is consumed
```

### 📄 Invoicing

```java
// Create invoice payment request
var invoice = client.incomingPayments()
    .create(request -> request
        .walletAddress(businessWallet)
        .incomingAmount(Amount.of("250000", "USD", 2)) // $2,500.00
        .expiresAt(Instant.now().plus(Duration.ofDays(30)))
        .metadata("{\"invoiceNumber\": \"INV-2025-001\", \"dueDate\": \"2025-03-01\"}"))
    .join();
```

## Core APIs

### Wallet Addresses

Get public account information:

```java
var wallet = client.walletAddresses()
    .get("https://wallet.example.com/alice")
    .join();

// Get public keys for verification
var keys = client.walletAddresses()
    .getKeys(wallet.id().toString())
    .join();
```

### Grants (Authorization)

Request permissions using GNAP protocol:

```java
var grant = client.grants()
    .request(request -> request
        .addAccessRight(AccessRight.builder()
            .type("incoming-payment")
            .actions(List.of("create", "read", "list"))
            .build()))
    .join();

if (grant.requiresInteraction()) {
    // Redirect user to grant.getInteractUrl() for approval
}
```

### Payment Lifecycle

Complete payment workflow:

```java
// 1. Create quote
var quote = client.quotes().create(...).join();

// 2. Create outgoing payment
var payment = client.outgoingPayments().create(...).join();

// 3. Monitor payment status
var updated = client.outgoingPayments().get(payment.getId()).join();

// 4. List all payments
var payments = client.outgoingPayments()
    .list(walletUrl, null, 50)
    .join();
```

## Architecture

The SDK is organized into clean, focused packages:

```
zm.hashcode.openpayments/
├── client/          - Main SDK entry point
├── auth/            - Authentication & authorization (GNAP)
├── wallet/          - Wallet address operations
├── payment/         - Payment operations
│   ├── incoming/    - Incoming payments
│   ├── outgoing/    - Outgoing payments
│   └── quote/       - Payment quotes
├── model/           - Common data models
├── http/            - HTTP abstraction layer
└── util/            - Utility classes
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed design documentation.

## Java 25 Features

This SDK leverages modern Java features for clean, maintainable code:

- **Records** - Immutable data models with less boilerplate
- **Var** - Local variable type inference
- **CompletableFuture** - Async operations
- **Optional** - Null safety
- **Functional Interfaces** - Clean callbacks

See [docs/JAVA_25_FEATURES.md](docs/JAVA_25_FEATURES.md) for details.

## Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/open-payments-java.git
cd open-payments-java

# Build the project
./gradlew build

# Run tests
./gradlew test

# Format code
./gradlew spotlessApply
```

## Documentation

📚 **[Documentation Index](docs/INDEX.md)** - Complete documentation guide with navigation help

**Quick Links**:
- **[Quick Reference](docs/QUICK_REFERENCE.md)** - Common operations and examples
- **[Architecture Decision Records](docs/ADR.md)** - Key design decisions and rationale
- **[Architecture Guide](docs/ARCHITECTURE.md)** - Runtime architecture and component interactions
- **[SDK Structure](docs/SDK_STRUCTURE.md)** - Package organization and file listing
- **[Java 25 Features](docs/JAVA_25_FEATURES.md)** - Modern Java usage with examples
- **[API Coverage](docs/API_COVERAGE.md)** - Complete Open Payments API mapping
- **[Code Quality](docs/CODE_QUALITY.md)** - Standards and tooling
- **[Setup Guide](docs/BUILD_SETUP_SUMMARY.md)** - Development environment

---

<a name="project-status"></a>
## 📊 Project Status

**[⬆️ Back to Top](#-quick-navigation)**

### Current Stage: Development (Pre-Release)

This SDK is currently under active development. While the core API structure is defined, implementations are not yet complete.

#### ✅ Completed
- High-level API interfaces and structure
- Java 25 record-based data models
- Complete Open Payments API coverage design
- Test stub framework (198+ test stubs)
- Documentation and architecture decisions
- Build tooling (Gradle, Checkstyle, Spotless)

#### 🚧 In Progress
- Service implementations
- HTTP client integration
- GNAP authorization flow implementation
- HTTP signature authentication
- Integration tests

#### 📋 Planned
- Complete unit test implementations
- Live API integration testing
- Performance optimization
- Maven Central publication
- JavaDoc hosting
- Version 1.0 release

### Versioning

This project follows [Semantic Versioning](https://semver.org/):
- **Current Version**: 0.1.0-SNAPSHOT
- **Target Stable Release**: 1.0.0
- **Java Compatibility**: Java 25+

### Roadmap

| Milestone | Target | Status |
|-----------|--------|--------|
| API Design & Documentation | M1     | ✅ Complete |
| Core Implementation | M2     | 🚧 In Progress |
| Integration Tests | M3     | 📋 Planned |
| Beta Release (0.9.0) | M4     | 📋 Planned |
| Stable Release (1.0.0) | M5     | 📋 Planned |

### Build Status

```bash
# Build the project
./gradlew build

# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew allTests
```

**[⬆️ Back to Top](#-quick-navigation)**

---

<a name="license"></a>
## 📜 License

**[⬆️ Back to Top](#-quick-navigation)**

### Apache License 2.0

Copyright 2025 Open Payments Java SDK Contributors

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.

**Key Points:**
- ✅ **Commercial Use** - Use freely in commercial projects
- ✅ **Modification** - Modify and distribute modified versions
- ✅ **Distribution** - Distribute original or modified versions
- ✅ **Patent Grant** - Express patent license included
- ⚠️ **Trademark** - No permission to use trademarks
- ⚠️ **Liability** - No warranty provided
- ⚠️ **Attribution** - Must include license and copyright notice

#### You are free to:
- Use the software for any purpose
- Change the software to suit your needs
- Share the software with anyone
- Share changes you make to the software

#### Under the following terms:
- **License and Copyright Notice**: Include a copy of the license and copyright notice with the software
- **State Changes**: If you modify the software, you must document your changes
- **No Trademark Use**: You cannot use the project's trademarks without permission
- **No Warranty**: The software is provided "as is" without warranties

#### Full License Text

For the complete license text, see [LICENSE](LICENSE) file.

**Apache 2.0** is one of the most permissive open-source licenses and is approved by the Open Source Initiative (OSI). It's compatible with GPL v3 and commonly used for Java projects.

**[⬆️ Back to Top](#-quick-navigation)**

---

<a name="contributing"></a>
## 🤝 Contributing

**[⬆️ Back to Top](#-quick-navigation)**

We welcome contributions from the community! Whether you're fixing bugs, adding features, improving documentation, or reporting issues, your help is appreciated.

### Quick Start for Contributors

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/boniface/open-payments-java.git
   cd open-payments-java
   ```
3. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```
4. **Make your changes** following our code style
5. **Run tests**:
   ```bash
   ./gradlew test
   ```
6. **Format code**:
   ```bash
   ./gradlew spotlessApply
   ```
7. **Commit with conventional commit messages**:
   ```bash
   git commit -m "feat: add new feature"
   ```
8. **Push and open a Pull Request**

### Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `style:` - Code style (formatting, etc.)
- `refactor:` - Code refactoring
- `test:` - Adding/updating tests
- `chore:` - Maintenance tasks

### Code Style

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters max
- **Braces**: Required for all control structures
- **Use Java 25 features**: Records, var, CompletableFuture, Optional
- **Documentation**: All public APIs must have JavaDoc

### Good First Issues

Look for issues labeled `good-first-issue` - these are great for new contributors:
- Documentation improvements
- Simple bug fixes
- Adding test coverage
- Code cleanup

### Development Guidelines

- ✅ Write tests for all new functionality
- ✅ Maintain or improve code coverage
- ✅ Update documentation when changing APIs
- ✅ Follow existing code patterns
- ✅ Keep commits focused and atomic
- ❌ Don't add unnecessary dependencies
- ❌ Don't break backwards compatibility without discussion

### Code Review Process

All contributions require review:
1. **Automated checks** must pass (build, tests, formatting)
2. **At least one approving review** from maintainers
3. **Documentation** must be updated
4. **No breaking changes** without major version bump

### Need Help?

- **Questions**: Open a [GitHub Discussion](https://github.com/boniface/open-payments-java/discussions)
- **Bug Reports**: Open an [Issue](https://github.com/boniface/open-payments-java/issues) with reproduction steps
- **Feature Requests**: Open an Issue to discuss before implementing

### Complete Contributing Guide

For detailed guidelines, see **[CONTRIBUTING.md](CONTRIBUTING.md)**

**[⬆️ Back to Top](#-quick-navigation)**

---

## Support

- **Documentation**: [https://openpayments.dev](https://openpayments.dev)
- **Issues**: [GitHub Issues](https://github.com/boniface/open-payments-java/issues)
- **Discussions**: [GitHub Discussions](https://github.com/boniface/open-payments-java/discussions)

## Acknowledgments

- [Open Payments Specification](https://openpayments.dev)
- [Interledger Foundation](https://interledger.org)
- [GNAP Protocol](https://datatracker.ietf.org/doc/html/draft-ietf-gnap-core-protocol)

## Related Projects

- [Open Payments PHP SDK](https://github.com/interledger/open-payments-php)
- [Open Payments TypeScript SDK](https://github.com/interledger/open-payments)
- [Open Payments Rust SDK](https://github.com/interledger/open-payments-rs)

---

**Status**: 🚧 Under Development | **Version**: 1.0-SNAPSHOT | **Java**: 25+
