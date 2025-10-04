# Contributing to Open Payments Java SDK

[🏠 Back to README](README.md)

---

Thank you for your interest in contributing! This document provides guidelines and instructions for contributing to the Open Payments Java SDK.

## Code of Conduct

This project adheres to a code of conduct. By participating, you are expected to uphold this code. Please be respectful and constructive in all interactions.

## Getting Started

### Prerequisites

- Java 25 or later
- Gradle 9.1 or later (wrapper included)
- Git
- A GitHub account

### Development Setup

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/open-payments-java.git
   cd open-payments-java
   ```

3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/open-payments-java.git
   ```

4. Build the project:
   ```bash
   ./gradlew build
   ```

## Development Workflow

### Before You Start

1. Check existing issues to avoid duplicates
2. For major changes, open an issue first to discuss
3. Make sure tests pass: `./gradlew test`
4. Ensure code is formatted: `./gradlew spotlessCheck`

### Making Changes

1. Create a new branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes following the code style guidelines below

3. Write or update tests for your changes

4. Run the build and tests:
   ```bash
   ./gradlew clean build
   ```

5. Format your code:
   ```bash
   ./gradlew spotlessApply
   ```

6. Commit your changes with a clear message:
   ```bash
   git add .
   git commit -m "feat: add support for XYZ"
   ```

### Commit Message Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `style:` - Code style changes (formatting, etc.)
- `refactor:` - Code refactoring
- `test:` - Adding or updating tests
- `chore:` - Maintenance tasks

Examples:
```
feat: add support for recurring payments
fix: handle null response in wallet service
docs: update README with async examples
test: add integration tests for grant flow
```

### Submitting Changes

1. Push your branch to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

2. Open a Pull Request on GitHub

3. Fill out the PR template with:
   - Description of changes
   - Related issue numbers
   - Testing performed

4. Wait for review and address any feedback

## Code Style Guidelines

### Java Style

We use Eclipse JDT formatter with Checkstyle validation. The configuration is automated:

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters max
- **Braces**: Required for all control structures
- **Naming**:
  - Classes: `PascalCase`
  - Methods/variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Packages: `lowercase`

### Java 25 Features

Leverage modern Java features where appropriate:

**Use Records** for immutable data models:
```java
public record Amount(String value, String assetCode, int assetScale) {
    public Amount {
        Objects.requireNonNull(value);
    }
}
```

**Use var** for obvious types:
```java
var client = OpenPaymentsClient.builder().build();
var amount = Amount.of("100", "USD", 2);
```

**Use Optional** for nullable values:
```java
public Optional<String> getMetadata() {
    return Optional.ofNullable(metadata);
}
```

**Use CompletableFuture** for async operations:
```java
CompletableFuture<WalletAddress> get(String url);
```

 **Avoid** raw types, unnecessary boxing, or outdated patterns

### Documentation

- All public APIs must have JavaDoc
- Include `@param`, `@return`, and `@throws` tags
- Provide code examples for complex APIs
- Keep documentation up-to-date with code changes

Example:
```java
/**
 * Creates a new incoming payment.
 *
 * <p>Incoming payments allow accounts to receive funds. The payment remains
 * open until completed or expired.
 *
 * @param requestBuilder a consumer to build the payment request
 * @return a CompletableFuture containing the created payment
 * @throws OpenPaymentsException if the request fails
 */
CompletableFuture<IncomingPayment> create(
    Consumer<IncomingPaymentRequest.Builder> requestBuilder
);
```

## Testing Guidelines

### Writing Tests

- Write tests for all new functionality
- Maintain or improve code coverage
- Use descriptive test names
- Follow AAA pattern (Arrange, Act, Assert)

Example:
```java
@Test
void shouldCreateIncomingPaymentWithValidRequest() {
    // Arrange
    var request = IncomingPaymentRequest.builder()
        .walletAddress(WALLET_URL)
        .incomingAmount(Amount.of("100", "USD", 2))
        .build();

    // Act
    var result = service.create(request).join();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getWalletAddress()).isEqualTo(WALLET_URL);
}
```

### Test Categories

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **Mock Tests**: Use MockWebServer for HTTP testing

### Running Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests WalletAddressServiceTest

# With coverage
./gradlew test jacocoTestReport
```

## Project Structure

```
open-payments-java/
├── src/
│   ├── main/java/zm/hashcode/openpayments/
│   │   ├── client/          # Main client API
│   │   ├── auth/            # Authentication
│   │   ├── wallet/          # Wallet operations
│   │   ├── payment/         # Payment operations
│   │   ├── model/           # Common models
│   │   ├── http/            # HTTP layer
│   │   └── util/            # Utilities
│   └── test/java/           # Tests mirror main structure
├── docs/                    # Documentation
├── config/                  # Build tool configs
└── build.gradle.kts         # Build configuration
```

## Code Review Process

All contributions require review before merging:

1. **Automated Checks**: Must pass (build, tests, formatting)
2. **Code Review**: At least one approving review required
3. **Documentation**: Ensure docs are updated
4. **Backwards Compatibility**: Avoid breaking changes when possible

### Review Checklist

- [ ] Code follows style guidelines
- [ ] Tests are included and passing
- [ ] Documentation is updated
- [ ] Commit messages follow convention
- [ ] No unnecessary dependencies added
- [ ] Performance impact considered
- [ ] Security implications reviewed

## Making Your First Contribution

Good first issues are labeled with `good-first-issue`. These are typically:

- Documentation improvements
- Simple bug fixes
- Adding tests
- Code cleanup

Don't hesitate to ask questions in the issue or PR!

## Common Tasks

### Adding a New API Endpoint

1. Define the model (record) in appropriate package
2. Add method to service interface
3. Implement in service class
4. Add tests
5. Update documentation

### Adding Dependencies

1. Add to `build.gradle.kts` in appropriate section
2. Document why it's needed in PR description
3. Ensure license compatibility (Apache 2.0 compatible)
4. Update dependency documentation if needed

### Updating Documentation

1. Make changes in `docs/` directory
2. Update README.md if needed
3. Generate JavaDoc: `./gradlew javadoc`
4. Verify links work

## Getting Help

- **Questions**: Open a discussion on GitHub Discussions
- **Bugs**: Open an issue with detailed reproduction steps
- **Features**: Open an issue to discuss before implementing
- **Security**: Email maintainers directly (see SECURITY.md)

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

## Recognition

Contributors are recognized in:
- GitHub contributors page
- Release notes
- CONTRIBUTORS.md file (coming soon)

---

Thank you for contributing to Open Payments Java SDK! We appreciate your time and effort in making this project better.
