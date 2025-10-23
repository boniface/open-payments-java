# Changelog

All notable changes to the Open Payments Java SDK will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- 📦 **Maven Central Publishing Setup**
  - Automated CI/CD publishing via GitHub Actions
  - Central Portal integration
  - Comprehensive release guide (RELEASE_GUIDE.md)
  - GPG artifact signing configuration
  - Automated GitHub Release creation
  - JavaDoc deployment to GitHub Pages
  - Publication verification in CI/CD pipeline

### Changed
- 🔢 **Versioning Strategy**
  - Changed from `1.0.0-SNAPSHOT` to `0.1.0` (pre-1.0 development)
  - Adopted semantic versioning with 0.x.y for initial development
  - Configured for release-only versions (no SNAPSHOT support)

- 🔧 **Publishing Configuration**
  - Fixed Maven Central Portal URL to `https://central.sonatype.com`
  - Updated to token-based authentication (Central Portal tokens)
  - Removed legacy OSSRH references and configurations
  - Removed duplicate version declarations in build files
  - Simplified publishing workflow 

- 📝 **Documentation**
  - Added publishing docs into single comprehensive RELEASE_GUIDE.md
  - Updated CI_CD_SETUP.md to reflect Central Portal approach
  - Updated GITHUB_ACTIONS_SETUP.md with correct secret names

- ⚙️ **CI/CD Workflow**
  - Updated release.yml for Central Portal authentication
  - Added GPG key import step using crazy-max/ghaction-import-gpg
  - Updated secret names: `CENTRAL_PORTAL_*` instead of `SONATYPE_*`
  - Added automatic pre-release flag for 0.x versions
  - Enhanced artifact verification steps

### Removed
- 🗑️ **Cleanup**
  - Removed all SNAPSHOT version references from codebase
  - Removed OSSRH (legacy Sonatype) documentation and references
  - Removed duplicate/redundant publishing documentation files
  - Removed old sunset `s01.oss.sonatype.org` endpoint references

## [0.1.0] - Initial Development

### Phase 1: Core Cryptography ✅
- Ed25519 key generation and management
- JWK (JSON Web Key) support
- SHA-256 content digest calculation
- Base64 encoding utilities
- 48 comprehensive unit tests

### Phase 2: HTTP Signatures ✅
- RFC 9421 HTTP Message Signatures implementation
- Automatic request signing with Ed25519
- Signature component selection (method, uri, content-digest)
- Signature verification support
- HttpSignatureService with fluent API
- 54 unit tests covering signing and verification

### Phase 3: Grant Management (GNAP) ✅
- Complete GNAP protocol implementation (RFC 9635)
- Grant request/response models with builder pattern
- Factory methods for common access types
- Interactive and non-interactive grant flows
- Grant continuation and cancellation
- 182 comprehensive unit tests
- Zero PMD violations

### Phase 4: Token Lifecycle ✅
- Token rotation for extending access
- Token revocation for cleanup
- Automatic GNAP authorization headers
- Error handling with structured exceptions
- 16 unit tests for token management
- Zero PMD violations

### Phase 5: HTTP Interceptors ✅
- LoggingRequestInterceptor with sensitive header masking
- LoggingResponseInterceptor with configurable log levels
- AuthenticationInterceptor supporting Bearer, GNAP, Basic, and Custom schemes
- ErrorHandlingInterceptor for structured error extraction
- 79 comprehensive unit tests
- Thread-safe implementations with ConcurrentHashMap
- Zero PMD violations in interceptor package

### Phase 6: Documentation & Polish ✅
- Comprehensive README.md with quick start guide
- USAGE_EXAMPLES.md with complete code examples
- Package-level documentation (package-info.java)
- CONTRIBUTING.md guidelines
- CHANGELOG.md
- Code quality improvements (PMD, Checkstyle compliance)

### Phase 7: Open Payments Resources ✅
- **7.1: OpenPaymentsClient Implementation**
  - DefaultOpenPaymentsClient with service accessors
  - DefaultOpenPaymentsClientBuilder with fluent API
  - 33 comprehensive unit tests
  - Thread-safe resource management

- **7.2: WalletAddressService Implementation**
  - DefaultWalletAddressService for wallet discovery
  - get(String/URI) for wallet address metadata
  - getKeys() for public key retrieval
  - 16 unit tests with mock HTTP responses

- **7.3: IncomingPaymentService Implementation**
  - DefaultIncomingPaymentService for receiving payments
  - create(), get(), list(), complete() operations
  - Cursor-based pagination support
  - 21 unit tests covering all CRUD operations

- **7.4: OutgoingPaymentService Implementation**
  - DefaultOutgoingPaymentService for sending payments
  - create(), get(), list() operations
  - Quote-based payment creation
  - 20 unit tests with comprehensive coverage

- **7.5: QuoteService Implementation**
  - DefaultQuoteService for exchange rate quotes
  - create() with sendAmount or receiveAmount
  - get() for quote retrieval
  - isExpired() convenience method
  - 19 unit tests

- **7.6: Integration and Documentation**
  - package-info.java for all service packages
  - Complete CODE_SNIPPETS.md with real-world scenarios (in docs/)
  - Updated INDEX.md with CODE_SNIPPETS documentation
  - Updated PROJECT_STATUS.md
  - Updated CHANGELOG.md

## Quality Metrics

### Test Coverage
- **Total Tests**: 465
- **Passing**: 465 (100%)
- **Failed**: 0
- **Skipped**: 14 (integration tests for Phase 8)

### Code Quality
- **PMD Main**: 0 violations in implemented phases
- **Checkstyle**: Compliant with project standards
- **Spotless**: Automatic code formatting applied

### Documentation
- JavaDoc coverage: 100% for public APIs
- Package documentation: All active packages documented
- Usage examples: Comprehensive real-world scenarios
- README: Quick start and feature overview

## Breaking Changes

None - this is the initial development release.

## Deprecations

None.

## Migration Guide

Not applicable for initial release.

## Known Issues

1. Integration tests for Phases 5+ are still pending implementation
2. Performance benchmarks not yet established

## Future Plans

### Version 0.2.0
- Complete integration test suite
- Performance optimization
- Additional authentication schemes
- Enhanced error recovery

### Version 1.0.0 (Stable Release)
- Production-ready release with API stability commitment
- Full Open Payments API coverage
- Performance benchmarks and optimization
- Comprehensive integration testing
- Production deployment guide
- First stable release on Maven Central

## Contributors

- Boniface Kabaso - Initial implementation
- Espoir Diteekemena - Initial implementation and Documentation

## References

- [Open Payments Specification](https://openpayments.dev)
- [GNAP Protocol - RFC 9635](https://datatracker.ietf.org/doc/html/rfc9635)
- [HTTP Signatures - RFC 9421](https://datatracker.ietf.org/doc/html/rfc9421)
- [JSON Web Key (JWK) - RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517)

---

**Legend**:
- ✅ Completed
- 🚧 In Progress
- 📋 Planned
- ⚠️ Known Issue
- 🔧 Bug Fix
- ✨ New Feature
- 📝 Documentation
- 🎨 Code Style
- ⚡ Performance
- 🔒 Security
