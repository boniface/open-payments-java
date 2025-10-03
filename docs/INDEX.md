# Documentation Index

[🏠 Back to README](../README.md)

---

This index helps you find the right document for your needs. Each document has a unique purpose with minimal overlap.

## Quick Navigation

| I want to... | Read this document |
|--------------|-------------------|
| Understand **why** design decisions were made | [ADR.md](ADR.md) |
| Understand **how** the system works at runtime | [ARCHITECTURE.md](ARCHITECTURE.md) |
| Find **where** code is organized | [SDK_STRUCTURE.md](SDK_STRUCTURE.md) |
| Learn **what** Java 25 features are used | [JAVA_25_FEATURES.md](JAVA_25_FEATURES.md) |
| Get started quickly with examples | [QUICK_REFERENCE.md](QUICK_REFERENCE.md) |
| Verify complete API coverage | [API_COVERAGE.md](API_COVERAGE.md) |
| Contribute to the project | [CONTRIBUTING.md](../CONTRIBUTING.md) |
| Set up development environment | [BUILD_SETUP_SUMMARY.md](BUILD_SETUP_SUMMARY.md) |
| Understand code quality tools | [CODE_QUALITY.md](CODE_QUALITY.md) |

---

## Document Purposes

### 📋 [ADR.md](ADR.md) - Architecture Decision Records
**Answers**: "Why did we choose X over Y?"

**Contents**:
- 14 architecture decision records
- Context, decision, rationale, consequences for each
- Alternatives considered and rejected
- Trade-offs explained

**Key Decisions Documented**:
- Why Java 25 (not 17 or 21)
- Why records (not classes)
- Why CompletableFuture (not Reactor)
- Why Apache HttpClient 5
- Why interface-based services
- Why builder pattern
- Why GNAP and HTTP signatures
- Why Jackson for JSON
- Why Checkstyle + Spotless
- Why Apache 2.0 license
- Why no Reactive Streams
- Why service-oriented packages
- Why immutability throughout

**Use this when**: Making similar decisions in your code or understanding project philosophy

---

### 🏗️ [ARCHITECTURE.md](ARCHITECTURE.md) - Runtime Architecture
**Answers**: "How does the system work at runtime?"

**Contents**:
- Component diagram showing runtime layers
- Request/response flow diagrams
- Thread safety and concurrency model
- Error handling architecture
- Security architecture (auth layers)
- Performance considerations (pooling, caching)

**Key Concepts**:
- Client → Service → HTTP → API flow
- Virtual threads usage
- Connection pooling strategy
- Exception hierarchy
- HTTP signature and GNAP integration

**Use this when**: Understanding how components interact or debugging runtime behavior

---

### 📦 [SDK_STRUCTURE.md](SDK_STRUCTURE.md) - Package Organization
**Answers**: "Where is the code for X?"

**Contents**:
- Complete package structure
- File listing by package (32 files)
- Package dependency graph
- Detailed package contents tables
- Naming conventions
- File statistics

**Key Information**:
- What's in each package (client/, auth/, wallet/, payment/, model/, http/, util/)
- How many files, what types (interface, record, class)
- Dependencies between packages
- Build commands

**Use this when**: Navigating the codebase or adding new files

---

### ☕ [JAVA_25_FEATURES.md](JAVA_25_FEATURES.md) - Modern Java Usage
**Answers**: "How do we use Java 25 features?"

**Contents**:
- Code examples for each feature
- Before/after comparisons (traditional class vs record)
- Practical usage patterns
- Feature adoption summary table
- Virtual threads examples
- Best practices

**Key Features Demonstrated**:
- Records with compact constructors
- var for type inference
- Optional for null safety
- CompletableFuture for async
- Functional interfaces
- Stream API
- Java Time API
- Text blocks

**Use this when**: Learning modern Java patterns or writing new code

---

### 🚀 [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Usage Examples
**Answers**: "How do I do X with the SDK?"

**Contents**:
- Complete code examples
- Common use cases (P2P, e-commerce, subscriptions, etc.)
- Step-by-step workflows
- Error handling examples
- Configuration examples

**Key Examples**:
- Client initialization
- Creating payments
- GNAP authorization flow
- Pagination
- Async composition

**Use this when**: Implementing SDK features in your application

---

### ✅ [API_COVERAGE.md](API_COVERAGE.md) - API Mapping
**Answers**: "Does the SDK support X operation?"

**Contents**:
- Complete mapping: API endpoint → SDK method
- 100% coverage verification (15/15 endpoints)
- All use cases explained
- Data model coverage
- Design patterns used
- Future enhancements (not in spec)

**Key Verification**:
- Wallet Address APIs: 2/2
- Incoming Payments: 4/4
- Outgoing Payments: 3/3
- Quotes: 2/2
- Grants/Tokens: 4/4

**Use this when**: Verifying SDK capabilities or finding which method to use

---

### 🧪 [CODE_QUALITY.md](CODE_QUALITY.md) - Standards & Tooling
**Answers**: "What quality tools are configured?"

**Contents**:
- Checkstyle configuration
- Spotless auto-formatting
- Code style rules
- Quality metrics
- Build integration

**Use this when**: Understanding code standards or configuring IDE

---

### 🛠️ [BUILD_SETUP_SUMMARY.md](BUILD_SETUP_SUMMARY.md) - Development Setup
**Answers**: "How do I set up the development environment?"

**Contents**:
- Prerequisites
- Build configuration
- Gradle setup
- Java 25 toolchain
- Dependency management

**Use this when**: Setting up a new development machine

---

### 🤝 [CONTRIBUTING.md](../CONTRIBUTING.md) - Contribution Guide
**Answers**: "How do I contribute?"

**Contents**:
- Development workflow
- Git commit conventions
- Pull request process
- Code review checklist
- Testing guidelines

**Use this when**: Contributing code or documentation

---

## Documentation Comparison Matrix

| Topic | ADR | ARCHITECTURE | SDK_STRUCTURE | JAVA_25_FEATURES |
|-------|-----|--------------|---------------|------------------|
| **Focus** | Why (rationale) | How (runtime) | Where (organization) | What (features) |
| **Design decisions** | ✅ Primary | ❌ Not covered | ❌ Not covered | ❌ Not covered |
| **Component diagrams** | ❌ Not covered | ✅ Primary | ❌ Not covered | ❌ Not covered |
| **Package structure** | ❌ Not covered | ✅ Brief | ✅ Primary | ❌ Not covered |
| **Code examples** | ✅ Brief | ✅ Flow examples | ❌ Not covered | ✅ Primary |
| **File listing** | ❌ Not covered | ❌ Not covered | ✅ Primary | ❌ Not covered |
| **Feature comparison** | ✅ Alternatives | ❌ Not covered | ❌ Not covered | ✅ Before/after |
| **Runtime flow** | ❌ Not covered | ✅ Primary | ❌ Not covered | ❌ Not covered |
| **Dependencies** | ✅ Why chosen | ✅ How used | ✅ Brief list | ❌ Not covered |

## Reading Order for New Contributors

1. **[README.md](../README.md)** - Project overview and quick start
2. **[SDK_STRUCTURE.md](SDK_STRUCTURE.md)** - Understand code organization
3. **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - See SDK in action
4. **[ARCHITECTURE.md](ARCHITECTURE.md)** - Understand runtime behavior
5. **[ADR.md](ADR.md)** - Learn why decisions were made
6. **[JAVA_25_FEATURES.md](JAVA_25_FEATURES.md)** - Learn coding patterns
7. **[CONTRIBUTING.md](../CONTRIBUTING.md)** - Start contributing

## Document Ownership

Each document answers a specific question type:

- **ADR**: Why questions (rationale, trade-offs, alternatives)
- **ARCHITECTURE**: How questions (runtime, flows, interactions)
- **SDK_STRUCTURE**: Where questions (packages, files, organization)
- **JAVA_25_FEATURES**: What questions (features, syntax, patterns)
- **QUICK_REFERENCE**: Usage questions (how to use, examples)
- **API_COVERAGE**: Coverage questions (what's supported, mapping)

---

**Last Updated**: 2025-10-02
**Document Count**: 9 markdown files
**Total Overlap**: Minimal (cross-references only)
