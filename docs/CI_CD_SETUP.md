# CI/CD Setup Guide

[🏠 Back to README](../README.md) | [📚 Documentation Index](INDEX.md)

---

This document describes the CI/CD pipeline configuration for the Open Payments Java SDK, following best practices for Maven Central releases.

## Overview

The project uses GitHub Actions for continuous integration, quality checks, and automated releases to Maven Central.

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

Triggers when a version tag is pushed (e.g., `v1.0.0`).

#### Steps
1. **Validation** - Gradle wrapper validation
2. **Quality Gates** - All CI checks must pass
3. **Build & Sign** - Artifacts signed with GPG
4. **Publish to Maven Central** - Via Sonatype OSSRH
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
- `SONATYPE_USERNAME` - Sonatype JIRA username
- `SONATYPE_PASSWORD` - Sonatype JIRA password
- `GPG_PRIVATE_KEY` - Base64 encoded GPG private key
- `GPG_PASSPHRASE` - GPG key passphrase

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

1. **Sonatype JIRA Account**
   - Create account at https://issues.sonatype.org
   - Request new project (OSSRH ticket)
   - Wait for approval (~2 business days)

2. **GPG Key for Signing**
   ```bash
   # Generate GPG key
   gpg --gen-key

   # List keys
   gpg --list-keys

   # Export private key (base64)
   gpg --export-secret-keys YOUR_KEY_ID | base64

   # Export public key to keyserver
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

3. **GitHub Secrets Configuration**
   - Add all required secrets to repository
   - Test with a snapshot release first

### Release Process

1. **Prepare Release**
   ```bash
   # Update version in build.gradle.kts
   version = "1.0.0"

   # Commit version change
   git add build.gradle.kts
   git commit -m "chore: prepare release 1.0.0"
   git push
   ```

2. **Create Release Tag**
   ```bash
   # Create and push tag
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

3. **Monitor Workflow**
   - Watch GitHub Actions for progress
   - Release workflow publishes to Maven Central
   - Verify artifact on https://repo1.maven.org/maven2/

4. **Post-Release**
   ```bash
   # Update to next snapshot version
   version = "1.1.0-SNAPSHOT"

   git add build.gradle.kts
   git commit -m "chore: prepare for next development iteration"
   git push
   ```

## Badge Updates

Update README badges with actual values once integrated:

```markdown
[![CI](https://github.com/boniface/open-payments-java/workflows/CI/badge.svg)](https://github.com/boniface/open-payments-java/actions)
[![codecov](https://codecov.io/gh/yourusername/open-payments-java/branch/main/graph/badge.svg)](https://codecov.io/gh/boniface/open-payments-java)
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
git commit -m "style: apply code formatting"
```

### Signing Fails
```bash
# Verify GPG key is configured
echo $GPG_PRIVATE_KEY | base64 -d | gpg --import

# Test signing locally
./gradlew signMavenJavaPublication
```

### Maven Central Sync Issues
- Wait 10-30 minutes after release
- Check https://repo1.maven.org/maven2/zm/hashcode/open-payments-java/
- Verify in Sonatype Nexus: https://s01.oss.sonatype.org/

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
- MAJOR: Breaking changes
- MINOR: New features (backward compatible)
- PATCH: Bug fixes (backward compatible)

## Resources

- [Maven Central Publishing Guide](https://central.sonatype.org/publish/publish-guide/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [SpotBugs Manual](https://spotbugs.readthedocs.io/)
- [PMD Rules Reference](https://pmd.github.io/latest/pmd_rules_java.html)
- [SonarCloud Documentation](https://docs.sonarcloud.io/)

---

[🏠 Back to README](../README.md) | [📚 Documentation Index](INDEX.md)
