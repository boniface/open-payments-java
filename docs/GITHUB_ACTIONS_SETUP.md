# GitHub Actions CI/CD Setup Summary

## What's Been Configured

This project has automated CI/CD following Maven Central Portal best practices.

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
Triggers on version tags (e.g., `v0.1.0`)

**Steps:**
- Run all quality checks
- Build and sign artifacts (GPG)
- Publish to Maven Central (Central Portal)
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
CENTRAL_PORTAL_USERNAME  - Central Portal token username
CENTRAL_PORTAL_PASSWORD  - Central Portal token password
GPG_PRIVATE_KEY         - ASCII armored GPG private key
GPG_PASSPHRASE          - Your GPG key passphrase
SIGNING_KEY_ID          - Last 8 characters of GPG key ID
```

### Code Coverage
```
CODECOV_TOKEN           - Token from codecov.io
```

### Code Quality
```
SONAR_TOKEN             - Token from sonarcloud.io
```

## Setup Checklist

### Before First Release

- [ ] **Create Central Portal account**
  - Register at https://central.sonatype.com
  - **Note:** `zm.hashcode` namespace is already verified ✓

- [ ] **Generate Central Portal token**
  - Go to https://central.sonatype.com/account
  - Click "Generate User Token"
  - Save username and password

- [ ] **Generate GPG key**
  ```bash
  gpg --gen-key
  gpg --export-secret-keys --armor YOUR_KEY_ID
  gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
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
  - Add all 7 secrets listed above
  - See [RELEASE_GUIDE.md](../RELEASE_GUIDE.md) for details

- [ ] **Update Repository Settings**
  - Enable GitHub Pages (Settings → Pages → Source: gh-pages branch)
  - Enable vulnerability alerts
  - Require status checks for PRs

### Update Badges in README

Replace placeholders with actual values:

```markdown
[![CI](https://github.com/boniface/open-payments-java/workflows/CI/badge.svg)](https://github.com/boniface/open-payments-java/actions)
[![codecov](https://codecov.io/gh/boniface/open-payments-java/branch/main/graph/badge.svg)](https://codecov.io/gh/boniface/open-payments-java)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=boniface_open-payments-java&metric=alert_status)](https://sonarcloud.io/dashboard?id=boniface_open-payments-java)
[![Maven Central](https://img.shields.io/maven-central/v/zm.hashcode/open-payments-java.svg)](https://search.maven.org/artifact/zm.hashcode/open-payments-java)
```

## Release Process

### 1. Prepare Release
```bash
# Update version in gradle.properties
version=0.1.0

git add gradle.properties
git commit -m "Release version 0.1.0"
git push
```

### 2. Create Tag
```bash
git tag -a v0.1.0 -m "Release version 0.1.0"
git push origin v0.1.0
```

### 3. Monitor
- Watch GitHub Actions tab
- Release workflow runs automatically
- Artifact published to Maven Central in ~15-30 minutes

### 4. Post-Release
```bash
# Bump to next version
version=0.2.0

git add gradle.properties
git commit -m "Bump version to 0.2.0"
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

See [docs/CI_CD_SETUP.md](CI_CD_SETUP.md) for detailed documentation.

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
