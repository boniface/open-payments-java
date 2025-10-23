# Maven Central Release Guide

Complete guide for publishing `open-payments-java` to Maven Central using automated CI/CD and the Central Portal.

---

## Table of Contents

- [Overview](#overview)
- [Versioning Strategy](#versioning-strategy)
- [Prerequisites (One-Time Setup)](#prerequisites-one-time-setup)
- [Automated Release Process](#automated-release-process)
- [Manual Release Process (Fallback)](#manual-release-process-fallback)
- [Troubleshooting](#troubleshooting)
- [CI/CD Integration](#cicd-integration)
- [Quick Reference](#quick-reference)

---

## Overview

This project is configured to publish to Maven Central via the **Central Portal** API:

- **Publishing Endpoint:** `https://central.sonatype.com`
- **Namespace:** `zm.hashcode` (already verified ✓)
- **Artifact:** `open-payments-java`
- **Group ID:** `zm.hashcode`
- **Authentication:** Token-based (no username/password)
- **Current Version:** `0.1.0` (pre-1.0 development)

**Publication Methods:**
1. **Automated (Recommended):** Push a git tag → GitHub Actions publishes automatically
2. **Manual (Fallback):** Run Gradle commands locally

---

## Versioning Strategy

We follow [Semantic Versioning 2.0.0](https://semver.org/) with a **pre-1.0.0 development phase**.

### Version Number Format: `MAJOR.MINOR.PATCH`

#### Pre-1.0 Development (Current)
- **0.x.y** = Initial development (API may change)
  - `0.1.0` = First release ← **YOU ARE HERE**
  - `0.2.0` = New features added
  - `0.3.0` = More features, bug fixes
  - Continue until API is stable...

#### Post-1.0 Stable
- **1.0.0** = First stable, production-ready release
  - Signals API stability commitment
  - Breaking changes require MAJOR version bump

- **1.x.y+** = Production releases
  - `1.1.0` = New backward-compatible features (MINOR)
  - `1.0.1` = Backward-compatible bug fixes (PATCH)
  - `2.0.0` = Breaking changes (MAJOR)

### When to Bump Versions

| Change Type | Current Phase (0.x) | Stable Phase (1.x+) | Example |
|-------------|---------------------|---------------------|---------|
| Breaking API changes | MINOR | MAJOR | 0.1.0 → 0.2.0 or 1.0.0 → 2.0.0 |
| New features | MINOR | MINOR | 0.2.0 → 0.3.0 or 1.0.0 → 1.1.0 |
| Bug fixes | PATCH | PATCH | 0.2.0 → 0.2.1 or 1.0.1 → 1.0.2 |
| API stabilization | MAJOR (→ 1.0.0) | N/A | 0.9.0 → 1.0.0 |

---

## Prerequisites (One-Time Setup)

### 1. Central Portal Account

**Register at:** https://central.sonatype.com

- Sign up with your GitHub account (recommended)
- Verify your email address
- **Note:** The `zm.hashcode` namespace is already verified and ready to use ✓

### 2. Generate Publishing Token

1. Go to https://central.sonatype.com/account
2. Click "**Generate User Token**"
3. Copy both the **username** and **password** (you'll need these later)

### 3. GPG Signing Setup

All artifacts published to Maven Central must be cryptographically signed with GPG.

#### Generate GPG Key

```bash
# Generate new GPG key (use default options)
gpg --gen-key

# Follow prompts:
# - Real name: Boniface Kabaso
# - Email: 550236+boniface@users.noreply.github.com
# - Passphrase: (choose a strong password - save it!)
```

#### List Keys and Get KEY_ID

```bash
# List your keys to get KEY_ID
gpg --list-keys

# Example output:
# pub   rsa3072 2025-01-15 [SC] [expires: 2027-01-15]
#       ABCD1234EFGH5678IJKL9012MNOP3456QRST7890  ← This is your KEY_ID
# uid   [ultimate] Boniface Kabaso <550236+boniface@users.noreply.github.com>

# The KEY_ID is the full 40-character fingerprint
# The SIGNING_KEY_ID is the last 8 characters (e.g., QRST7890)
```

#### Export Public Key to Key Servers (REQUIRED)

```bash
# Export to multiple key servers (for redundancy)
gpg --keyserver keys.openpgp.org --send-keys ABCD1234EFGH5678IJKL9012MNOP3456QRST7890
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234EFGH5678IJKL9012MNOP3456QRST7890

# Verify it was uploaded (wait a few minutes)
gpg --keyserver keys.openpgp.org --recv-keys ABCD1234EFGH5678IJKL9012MNOP3456QRST7890
```

### 4. Configure GitHub Secrets

**For Automated Releases (Recommended)**

Go to: `https://github.com/boniface/open-payments-java/settings/secrets/actions`

Add these **5 required secrets**:

| Secret Name | Value | How to Get |
|-------------|-------|------------|
| `GPG_PRIVATE_KEY` | ASCII armored GPG private key | `gpg --export-secret-keys --armor YOUR_KEY_ID` |
| `GPG_PASSPHRASE` | Your GPG key passphrase | The password you chose during `gpg --gen-key` |
| `SIGNING_KEY_ID` | Last 8 characters of GPG key ID | From `gpg --list-keys` (e.g., `QRST7890`) |
| `CENTRAL_PORTAL_USERNAME` | Central Portal token username | From step 2 above |
| `CENTRAL_PORTAL_PASSWORD` | Central Portal token password | From step 2 above |

**Example: Getting GPG_PRIVATE_KEY**

```bash
# Export private key in ASCII armor format
gpg --export-secret-keys --armor ABCD1234EFGH5678IJKL9012MNOP3456QRST7890

# Copy the ENTIRE output (including BEGIN/END lines):
# -----BEGIN PGP PRIVATE KEY BLOCK-----
# ...
# -----END PGP PRIVATE KEY BLOCK-----
```

### 5. Configure Local Credentials (For Manual Releases)

**Option A: User-level configuration** (recommended - keeps secrets out of repo)

Create/edit `~/.gradle/gradle.properties`:

```properties
# GPG Signing
signing.keyId=QRST7890
signing.password=your-gpg-passphrase
signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg

# Central Portal Authentication
centralPortalUsername=your-token-username
centralPortalPassword=your-token-password
```

**Export Secret Key for Gradle (GPG 2.1+)**

```bash
# Export to legacy format for Gradle
gpg --export-secret-keys > ~/.gnupg/secring.gpg
```

---

## Automated Release Process

### Overview

Push a git tag → GitHub Actions automatically publishes to Maven Central.

**Timeline:**
- GitHub Actions workflow: ~5-10 minutes
- Maven Central sync: ~15-30 minutes
- **Total:** ~30-40 minutes until publicly available

### Step 1: Update Version

Edit `gradle.properties`:

```properties
# For first release (already set):
version=0.1.0

# For subsequent releases:
version=0.2.0  # New features
version=0.1.1  # Bug fix
version=1.0.0  # Stable release
```

### Step 2: Update CHANGELOG (Optional but Recommended)

Document what changed in this release:

```markdown
## [0.1.0] - 2025-01-22

### Added
- Initial implementation of Open Payments client
- Support for wallet addresses, quotes, and payments
- GNAP authorization flow
- HTTP signature authentication

### Fixed
- None (first release)

### Changed
- None (first release)
```

### Step 3: Run Tests Locally

```bash
# Run all tests
./gradlew test

# Build artifacts
./gradlew build

# Verify coverage
./gradlew jacocoTestCoverageVerification

# Check for PMD violations
./gradlew pmdMain
```

### Step 4: Commit and Tag

```bash
# Commit version change
git add gradle.properties CHANGELOG.md
git commit -m "Release version 0.1.0"

# Create annotated tag (version must match gradle.properties)
git tag -a v0.1.0 -m "Release version 0.1.0"

# Push commits and tag to trigger release
git push origin feature/maven-publish
git push origin v0.1.0
```

### Step 5: Monitor the Release

1. **GitHub Actions:** Watch the workflow run
   ```
   https://github.com/boniface/open-payments-java/actions
   ```

2. **Central Portal:** Check upload status (after ~5-10 minutes)
   ```
   https://central.sonatype.com/publishing
   ```

3. **Maven Central:** Verify publication (after ~15-30 minutes)
   ```
   https://central.sonatype.com/artifact/zm.hashcode/open-payments-java
   https://search.maven.org/artifact/zm.hashcode/open-payments-java
   ```

### Step 6: Verify Publication

```bash
# Check if artifact is available (wait 15-30 minutes after release)
curl -I "https://repo1.maven.org/maven2/zm/hashcode/open-payments-java/0.1.0/open-payments-java-0.1.0.pom"

# Should return: HTTP/1.1 200 OK
```

### Step 7: Post-Release

#### Update to Next Version

Edit `gradle.properties`:

```properties
# Bump to next version
version=0.2.0
```

```bash
git add gradle.properties
git commit -m "Bump version to 0.2.0"
git push origin feature/maven-publish
```

#### Test Installation

Create a test project to verify:

```kotlin
// build.gradle.kts
dependencies {
    implementation("zm.hashcode:open-payments-java:0.1.0")
}
```

```bash
./gradlew build --refresh-dependencies
```

---

## Manual Release Process (Fallback)

If CI/CD fails or you need to publish manually:

### Step 1: Prepare Release

```bash
# Update version in gradle.properties
version=0.1.0

# Commit and tag
git add gradle.properties
git commit -m "Release version 0.1.0"
git tag -a v0.1.0 -m "Release version 0.1.0"
git push origin main
git push origin v0.1.0
```

### Step 2: Build and Test

```bash
# Clean build
./gradlew clean

# Run all tests
./gradlew test

# Build all artifacts (main JAR, sources JAR, javadoc JAR)
./gradlew build

# Verify artifacts were created
ls -lh build/libs/
# Should see:
# - open-payments-java-0.1.0.jar
# - open-payments-java-0.1.0-sources.jar
# - open-payments-java-0.1.0-javadoc.jar
```

### Step 3: Publish to Central Portal

Ensure you have configured `~/.gradle/gradle.properties` with credentials (see Prerequisites).

```bash
# Publish to Maven Central (signs and uploads)
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository

# Or in separate steps for more control:
./gradlew publishToSonatype                          # Upload to staging
./gradlew closeAndReleaseSonatypeStagingRepository   # Release to Central
```

**What happens:**
1. Gradle builds all artifacts (JAR, sources, javadoc)
2. Signs each artifact with your GPG key
3. Uploads to Central Portal staging repository
4. Validates artifacts (POM, signatures, required files)
5. Releases to Maven Central (public within 15-30 minutes)

### Step 4: Create GitHub Release (Manual)

```bash
# Via GitHub CLI
gh release create v0.1.0 \
  --title "Release 0.1.0" \
  --notes "Initial release of Open Payments Java SDK" \
  build/libs/open-payments-java-0.1.0.jar \
  build/libs/open-payments-java-0.1.0-sources.jar \
  build/libs/open-payments-java-0.1.0-javadoc.jar

# Or via web interface:
# https://github.com/boniface/open-payments-java/releases/new
```

---

## Troubleshooting

### ❌ "No value has been specified for property 'signing.keyId'"

**Problem:** GPG signing not configured

**Solution:**
```bash
# Configure in ~/.gradle/gradle.properties
signing.keyId=QRST7890
signing.password=your-gpg-passphrase
signing.secretKeyRingFile=/Users/yourusername/.gnupg/secring.gpg
```

### ❌ "Unable to find secret key"

**Problem:** Secret keyring not found

**Solution:**
```bash
# Export secret key to legacy format
gpg --export-secret-keys > ~/.gnupg/secring.gpg

# Verify location
ls -lh ~/.gnupg/secring.gpg
```

### ❌ Publishing Fails with "401 Unauthorized"

**Problem:** Invalid or expired Central Portal token

**Solution:**
1. Go to https://central.sonatype.com/account
2. Click "Generate User Token" (revokes old token)
3. Update credentials:
   - **Local:** Update `~/.gradle/gradle.properties`
   - **CI/CD:** Update GitHub secrets `CENTRAL_PORTAL_USERNAME` and `CENTRAL_PORTAL_PASSWORD`

### ❌ Signature Verification Fails

**Problem:** GPG public key not on key servers

**Solution:**
```bash
# Re-upload to key servers
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Wait a few minutes, then verify
gpg --keyserver keys.openpgp.org --recv-keys YOUR_KEY_ID
```

### ❌ Artifacts Not Appearing on Maven Central

**Problem:** Artifact not showing after 30+ minutes

**Solution:**
1. Check Central Portal status: https://central.sonatype.com/publishing
2. Look for validation errors in the publishing log
3. Check direct repository: `https://repo1.maven.org/maven2/zm/hashcode/open-payments-java/`
4. Contact Central Portal support if issue persists: https://central.sonatype.com/support

### ❌ Build Fails in CI/CD

**Problem:** Tests fail or build errors in GitHub Actions

**Solution:**
```bash
# Run locally to debug
./gradlew clean test build --stacktrace

# Fix issues, commit, and re-tag:
git tag -d v0.1.0                     # Delete local tag
git push origin :refs/tags/v0.1.0    # Delete remote tag

# After fixes:
git tag -a v0.1.0 -m "Release version 0.1.0"
git push origin v0.1.0
```

---

## CI/CD Integration

### GitHub Actions Workflow

The release workflow (`.github/workflows/release.yml`) is triggered when you push a git tag matching `v*.*.*`.

**What it does:**
1. ✅ Validates Gradle wrapper
2. ✅ Runs all quality checks (tests, coverage, PMD, SpotBugs)
3. ✅ Builds all artifacts (JAR, sources, javadoc)
4. ✅ Imports GPG key for signing
5. ✅ Signs artifacts with GPG
6. ✅ Publishes to Maven Central via Central Portal
7. ✅ Creates GitHub Release with artifacts
8. ✅ Deploys JavaDoc to GitHub Pages
9. ✅ Verifies artifact availability on Maven Central

**Required GitHub Secrets:**
- `GPG_PRIVATE_KEY` - ASCII armored GPG private key
- `GPG_PASSPHRASE` - GPG key passphrase
- `SIGNING_KEY_ID` - Last 8 characters of GPG key ID
- `CENTRAL_PORTAL_USERNAME` - Central Portal token username
- `CENTRAL_PORTAL_PASSWORD` - Central Portal token password

---

## Quick Reference

### Release Checklist

- [ ] All tests passing: `./gradlew test`
- [ ] Code formatted: `./gradlew spotlessApply`
- [ ] No PMD violations: `./gradlew pmdMain`
- [ ] Coverage meets threshold: `./gradlew jacocoTestCoverageVerification`
- [ ] CHANGELOG.md updated
- [ ] Version bumped in `gradle.properties`
- [ ] Committed changes: `git commit -m "Release version X.Y.Z"`
- [ ] Created git tag: `git tag -a vX.Y.Z -m "Release version X.Y.Z"`
- [ ] Pushed to remote: `git push origin branch && git push origin vX.Y.Z`
- [ ] GitHub Actions workflow succeeded
- [ ] Verified on Central Portal (after ~10 min)
- [ ] Verified on Maven Central (after ~30 min)
- [ ] Tested installation in sample project
- [ ] Version bumped to next development version

### Common Commands

```bash
# Automated Release
git add gradle.properties CHANGELOG.md
git commit -m "Release version 0.1.0"
git tag -a v0.1.0 -m "Release version 0.1.0"
git push origin feature/maven-publish && git push origin v0.1.0

# Manual Release
./gradlew clean test build
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository

# Verify Publication
curl -I "https://repo1.maven.org/maven2/zm/hashcode/open-payments-java/0.1.0/open-payments-java-0.1.0.pom"

# Monitor Release
open https://github.com/boniface/open-payments-java/actions
open https://central.sonatype.com/publishing
open https://central.sonatype.com/artifact/zm.hashcode/open-payments-java
```

### Version Examples

```properties
# Pre-1.0 releases
version=0.1.0  # First release
version=0.2.0  # New features
version=0.1.1  # Bug fix
version=1.0.0  # API stabilization

# Post-1.0 releases
version=1.1.0  # New features
version=1.0.1  # Bug fix
version=2.0.0  # Breaking changes
```

### Important Links

| Resource | URL |
|----------|-----|
| **Central Portal** | https://central.sonatype.com |
| **Account/Tokens** | https://central.sonatype.com/account |
| **Publishing Status** | https://central.sonatype.com/publishing |
| **Namespace Management** | https://central.sonatype.com/publishing/namespaces |
| **Maven Central Search** | https://search.maven.org/artifact/zm.hashcode/open-payments-java |
| **Direct Repository** | https://repo1.maven.org/maven2/zm/hashcode/open-payments-java/ |
| **GitHub Actions** | https://github.com/boniface/open-payments-java/actions |
| **GitHub Secrets** | https://github.com/boniface/open-payments-java/settings/secrets/actions |
| **Central Portal Docs** | https://central.sonatype.org/publish/publish-portal-gradle/ |
| **GPG Guide** | https://central.sonatype.org/publish/requirements/gpg/ |
| **Support** | https://central.sonatype.com/support |

---

## Resources

- **Central Portal Documentation:** https://central.sonatype.org/publish/publish-portal-gradle/
- **Gradle Nexus Publish Plugin:** https://github.com/gradle-nexus/publish-plugin
- **GPG Signing Guide:** https://central.sonatype.org/publish/requirements/gpg/
- **Semantic Versioning:** https://semver.org/
- **Project Issues:** https://github.com/boniface/open-payments-java/issues

---

**Last Updated:** 2025-10-22
**Publishing Method:** Maven Central Portal (current standard)
**Namespace:** `zm.hashcode` (verified ✓)
**Current Version:** `0.1.0`
