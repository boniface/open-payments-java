# GitHub Actions CI/CD Setup Summary

## What's Been Configured

This project has  CI/CD following Maven Central.

## Workflows Created

### 1. **CI Workflow** (`.github/workflows/ci.yml`)
Runs on every push and PR to `main`/`develop`

**Quality Checks:**
- Code formatting (Spotless)
- Code style (Checkstyle)
- Build compilation
- Unit tests
- Integration tests
- Code coverage (JaCoCo) - 80% minimum
- Dependency security (OWASP)
- Static analysis (SpotBugs, PMD)
- Code quality (SonarCloud)

**Build Matrix:**
- Ubuntu, macOS, Windows
- Java 25

### 2. **Release Workflow** (`.github/workflows/release.yml`)
Triggers on version tags (e.g., `v1.0.0`)

**Steps:**
- Run all quality checks
- Build and sign artifacts (GPG)
- Publish to Maven Central (Sonatype OSSRH)
- Create GitHub Release
- Deploy JavaDoc to GitHub Pages
- Verify Maven Central availability

### 3. **CodeQL Security** (`.github/workflows/codeql.yml`)
Runs on push, PR, and weekly schedule

**Features:**
- Security vulnerability scanning
- Code quality analysis
- GitHub Security Alerts integration

## Build Configuration Added

### Plugins
- `jacoco` - Code coverage
- `spotbugs` - Static analysis
- `pmd` - Code quality
- `dependencycheck` - Security vulnerabilities
- `sonarqube` - Continuous quality monitoring

### Quality Gates
```kotlin
// Coverage: 80% overall, 70% per class
jacocoTestCoverageVerification {
    violationRules {
        rule { minimum = 0.80 }
        rule { element = "CLASS"; minimum = 0.70 }
    }
}

// Security: Fail on CVSS >= 7.0
dependencyCheck {
    failBuildOnCVSS = 7.0f
}

// Style: Zero tolerance
checkstyle {
    maxWarnings = 0
}
```

## Required Secrets (To Be Added)

Configure these in **GitHub Settings → Secrets and variables → Actions**:

### Maven Central Publishing
```
SONATYPE_USERNAME     - Your Sonatype JIRA username
SONATYPE_PASSWORD     - Your Sonatype JIRA password
GPG_PRIVATE_KEY       - Base64 encoded GPG private key
GPG_PASSPHRASE        - Your GPG key passphrase
```

### Code Coverage
```
CODECOV_TOKEN         - Token from codecov.io
```

### Code Quality
```
SONAR_TOKEN           - Token from sonarcloud.io
```

## Setup Checklist

### Before First Release

- [ ] **Create Sonatype JIRA account**
  - Register at https://issues.sonatype.org
  - Create New Project ticket for `zm.hashcode`
  - Wait for approval (~2 business days)

- [ ] **Generate GPG key**
  ```bash
  gpg --gen-key
  gpg --export-secret-keys YOUR_KEY_ID | base64 > private-key.txt
  gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
  ```

- [ ] **Setup Codecov**
  - Sign up at https://codecov.io with GitHub
  - Add repository
  - Copy upload token

- [ ] **Setup SonarCloud**
  - Sign up at https://sonarcloud.io with GitHub
  - Create new project
  - Copy authentication token
  - Update `sonar.projectKey` and `sonar.organization` in build.gradle.kts

- [ ] **Configure GitHub Secrets**
  - Add all 6 secrets listed above
  - Test with a snapshot build first

- [ ] **Update Repository Settings**
  - Enable GitHub Pages (Settings → Pages → Source: gh-pages branch)
  - Enable vulnerability alerts
  - Require status checks for PRs

### Update Badges in README

Replace placeholders with actual values:

```markdown
[![CI](https://github.com/YOUR_USERNAME/open-payments-java/workflows/CI/badge.svg)](https://github.com/YOUR_USERNAME/open-payments-java/actions)
[![codecov](https://codecov.io/gh/YOUR_USERNAME/open-payments-java/branch/main/graph/badge.svg)](https://codecov.io/gh/YOUR_USERNAME/open-payments-java)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=YOUR_ORG_open-payments-java&metric=alert_status)](https://sonarcloud.io/dashboard?id=YOUR_ORG_open-payments-java)
[![Maven Central](https://img.shields.io/maven-central/v/zm.hashcode/open-payments-java.svg)](https://search.maven.org/artifact/zm.hashcode/open-payments-java)
```

## Release Process

### 1. Prepare Release
```bash
# Update version in build.gradle.kts
version = "1.0.0"

git add build.gradle.kts
git commit -m "chore: prepare release 1.0.0"
git push
```

### 2. Create Tag
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### 3. Monitor
- Watch GitHub Actions tab
- Release workflow runs automatically
- Artifact published to Maven Central in ~10-30 minutes

### 4. Post-Release
```bash
# Bump to next snapshot version
version = "1.1.0-SNAPSHOT"

git add build.gradle.kts
git commit -m "chore: prepare for next development iteration"
git push
```

## Local Testing

Run quality checks locally before pushing:

```bash
# Format code
./gradlew spotlessApply

# Run all checks
./gradlew clean build check

# Run tests with coverage
./gradlew test integrationTest jacocoTestReport

# Verify coverage meets threshold
./gradlew jacocoTestCoverageVerification

# View coverage report
open build/reports/jacoco/test/html/index.html

# Run security check
./gradlew dependencyCheckAnalyze

# Run static analysis
./gradlew spotbugsMain pmdMain
```

## Quality Metrics

After setup, you'll have:

- **Test Coverage**: Tracked on Codecov with PR comments
- **Code Quality**: Monitored on SonarCloud with quality gates
- **Security**: OWASP dependency checks + CodeQL weekly scans
- **Build Status**: Multi-OS build verification
- **Release Automation**: One-command Maven Central publishing

## 📚 Documentation

See [docs/CI_CD_SETUP.md](docs/CI_CD_SETUP.md) for detailed documentation.

## What Gets Checked on Every PR

1. Code formatted correctly (Spotless)
2. Follows code style (Checkstyle)
3. Builds on Ubuntu, macOS, Windows
4. All tests pass
5. Coverage ≥ 80%
6. No high-severity vulnerabilities
7. No critical bugs (SpotBugs/PMD)
8. Passes SonarCloud quality gate

**All checks must pass before merge.**

## Next Steps

1. Add GitHub secrets
2. Setup external services (Codecov, SonarCloud)
3. Push code and verify CI runs
4. Create first release tag when ready

---

**Status**: CI/CD infrastructure ready | **Next**: Configure secrets
