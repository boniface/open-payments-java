# CI/CD Setup Guide

[🏠 Back to README](../README.md) | [📚 Documentation Index](INDEX.md)

---

This document describes the CI/CD pipeline configuration for the Open Payments Java SDK, following best practices for Maven Central releases.

## Overview

The project uses GitHub Actions for continuous integration, quality checks, and automated releases to Maven Central via the **Central Portal**.

## Workflows

### 1. CI Workflow (`.github/workflows/ci.yml`)

Runs on every push and pull request to `main` and `develop` branches.

#### Build Matrix
- **Operating Systems**: Ubuntu, macOS, Windows
- **Java Version**: 25 (Temurin distribution)

#### Quality Checks
1. **Code Formatting** - Spotless validation
2. **Code Style** - Checkstyle validation
3. **Build** - Gradle build
4. **Unit Tests** - JUnit 5 tests
5. **Integration Tests** - Tagged with `@Tag("integration")`
6. **Code Coverage** - JaCoCo with 80% minimum
7. **Dependency Security** - OWASP Dependency Check
8. **Static Analysis** - SpotBugs and PMD
9. **Security Scanning** - SonarCloud analysis

#### Artifacts
- Test results
- Coverage reports (uploaded to Codecov)
- Dependency check reports
- SpotBugs and PMD reports

### 2. Release Workflow (`.github/workflows/release.yml`)

Triggers when a version tag is pushed (e.g., `v0.1.0`).

#### Steps
1. **Validation** - Gradle wrapper validation
2. **Quality Gates** - All CI checks must pass
3. **Build & Sign** - Artifacts signed with GPG
4. **Publish to Maven Central** - Via Central Portal
5. **GitHub Release** - Create release with artifacts
6. **JavaDoc Deployment** - Publish to GitHub Pages
7. **Verification** - Confirm artifact availability on Maven Central

### 3. CodeQL Security Analysis (`.github/workflows/codeql.yml`)

Runs on:
- Push to main/develop
- Pull requests
- Weekly schedule (Mondays at 00:00 UTC)

Analyzes code for security vulnerabilities and quality issues.

## Required GitHub Secrets

Configure these secrets in your GitHub repository settings:

### Maven Central Publishing
- `CENTRAL_PORTAL_USERNAME` - Central Portal token username
- `CENTRAL_PORTAL_PASSWORD` - Central Portal token password
- `GPG_PRIVATE_KEY` - ASCII armored GPG private key
- `GPG_PASSPHRASE` - GPG key passphrase
- `SIGNING_KEY_ID` - Last 8 characters of GPG key ID

### Code Coverage
- `CODECOV_TOKEN` - Codecov.io token for coverage reports

### Code Quality
- `SONAR_TOKEN` - SonarCloud authentication token

## Quality Gates

### Coverage Requirements
- **Overall Coverage**: 80% minimum
- **Per-Class Coverage**: 70% minimum
- **Exclusions**: package-info.java, module-info.java

### Static Analysis
- **SpotBugs**: Max effort, low confidence threshold
- **PMD**: Custom ruleset (config/pmd/ruleset.xml)
- **Checkstyle**: Google Java Style with modifications

### Security
- **OWASP Dependency Check**: Fails on CVSS >= 7.0
- **CodeQL**: Weekly security scans
- **SonarCloud**: Continuous quality monitoring

## Local Development

### Running Quality Checks Locally

```bash
# Format code
./gradlew spotlessApply

# Check formatting
./gradlew spotlessCheck

# Run Checkstyle
./gradlew checkstyleMain checkstyleTest

# Run all tests with coverage
./gradlew test integrationTest jacocoTestReport

# Verify coverage meets 80% threshold
./gradlew jacocoTestCoverageVerification

# Run SpotBugs
./gradlew spotbugsMain

# Run PMD
./gradlew pmdMain

# Run dependency security check
./gradlew dependencyCheckAnalyze

# Run all quality checks
./gradlew clean build check
```

### View Coverage Reports

After running tests:
```bash
open build/reports/jacoco/test/html/index.html
```

## Maven Central Setup

### Prerequisites

1. **Central Portal Account**
   - Register at https://central.sonatype.com
   - Sign up with GitHub (recommended)
   - **Note:** The `zm.hashcode` namespace is already verified

2. **Central Portal Token**
   - Go to https://central.sonatype.com/account
   - Click "Generate User Token"
   - Save username and password as GitHub secrets

3. **GPG Key for Signing**
   ```bash
   # Generate GPG key
   gpg --gen-key

   # List keys
   gpg --list-keys

   # Export private key (ASCII armored)
   gpg --export-secret-keys --armor YOUR_KEY_ID

   # Export public key to keyservers
   gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

4. **GitHub Secrets Configuration**
   - Add all required secrets to repository
   - See [RELEASE_GUIDE.md](../RELEASE_GUIDE.md) for detailed setup

### Release Process

1. **Prepare Release**
   ```bash
   # Update version in gradle.properties
   version=0.1.0

   # Commit version change
   git add gradle.properties
   git commit -m "Release version 0.1.0"
   git push
   ```

2. **Create Release Tag**
   ```bash
   # Create and push tag
   git tag -a v0.1.0 -m "Release version 0.1.0"
   git push origin v0.1.0
   ```

3. **Monitor Workflow**
   - Watch GitHub Actions for progress
   - Release workflow publishes to Maven Central
   - Verify artifact on https://central.sonatype.com/artifact/zm.hashcode/open-payments-java

4. **Post-Release**
   ```bash
   # Update to next version
   version=0.2.0

   git add gradle.properties
   git commit -m "Bump version to 0.2.0"
   git push
   ```

## Badge Updates

Update README badges with actual values once integrated:

```markdown
[![CI](https://github.com/boniface/open-payments-java/workflows/CI/badge.svg)](https://github.com/boniface/open-payments-java/actions)
[![codecov](https://codecov.io/gh/boniface/open-payments-java/branch/main/graph/badge.svg)](https://codecov.io/gh/boniface/open-payments-java)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=boniface_open-payments-java&metric=alert_status)](https://sonarcloud.io/dashboard?id=boniface_open-payments-java)
[![Maven Central](https://img.shields.io/maven-central/v/zm.hashcode/open-payments-java.svg)](https://search.maven.org/artifact/zm.hashcode/open-payments-java)
```

## Troubleshooting

### Coverage Check Fails
```bash
# Run coverage report to see details
./gradlew jacocoTestReport

# View HTML report
open build/reports/jacoco/test/html/index.html

# Identify uncovered code and add tests
```

### Formatting Check Fails
```bash
# Auto-format all code
./gradlew spotlessApply

# Commit formatted code
git add .
git commit -m "Apply code formatting"
```

### Signing Fails
```bash
# Verify GPG key is configured
echo $GPG_PRIVATE_KEY | gpg --import

# Test signing locally
./gradlew signMavenJavaPublication
```

### Maven Central Sync Issues
- Wait 15-30 minutes after release
- Check https://repo1.maven.org/maven2/zm/hashcode/open-payments-java/
- Verify in Central Portal: https://central.sonatype.com/publishing

## Best Practices

### Commit Messages
Follow [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation changes
- `style:` - Code formatting
- `refactor:` - Code restructuring
- `test:` - Test additions/changes
- `chore:` - Build/tooling changes

### Pull Requests
- All checks must pass (green)
- Coverage must not decrease
- At least one approval required
- Squash and merge preferred

### Versioning
Follow [Semantic Versioning](https://semver.org/):
- **0.x.y** - Pre-1.0 development (API may change)
- **1.0.0** - First stable release
- **MAJOR** - Breaking changes
- **MINOR** - New features (backward compatible)
- **PATCH** - Bug fixes (backward compatible)

## Resources

- [Maven Central Portal Documentation](https://central.sonatype.org/publish/publish-portal-gradle/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [SpotBugs Manual](https://spotbugs.readthedocs.io/)
- [PMD Rules Reference](https://pmd.github.io/latest/pmd_rules_java.html)
- [SonarCloud Documentation](https://docs.sonarcloud.io/)

---

[🏠 Back to README](../README.md) | [📚 Documentation Index](INDEX.md)
