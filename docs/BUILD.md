# Build Configuration & Developer Guide

**Answers**: "How do I build, test, and maintain this project?"

[🏠 Back to Index](INDEX.md)

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Build System Overview](#build-system-overview)
3. [Common Tasks](#common-tasks)
4. [Code Quality Tools](#code-quality-tools)
5. [Dependency Management](#dependency-management)
6. [Java 25 Support](#java-25-support)
7. [Troubleshooting](#troubleshooting)

---

## Quick Start

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Format code
./gradlew spotlessApply

# Check for dependency updates
./check-updates.sh
```

---

## Build System Overview

### Gradle Convention Plugins

The project uses **Gradle Convention Plugins** for modular build configuration:

```
project/
├── build.gradle.kts                    # Main build file (177 lines)
├── buildSrc/                           # Convention plugins
│   ├── build.gradle.kts                # Plugin dependencies
│   └── src/main/kotlin/
│       ├── dependencies-convention.gradle.kts      # All dependencies
│       ├── testing-convention.gradle.kts           # Test configuration
│       ├── quality-convention.gradle.kts           # Spotless & Checkstyle
│       ├── static-analysis-convention.gradle.kts   # PMD & SpotBugs
│       ├── coverage-convention.gradle.kts          # JaCoCo coverage
│       ├── security-convention.gradle.kts          # OWASP checks
│       ├── sonar-convention.gradle.kts             # SonarQube
│       ├── publishing-convention.gradle.kts        # Maven Central
│       └── utilities-convention.gradle.kts         # Dependency updates
```

**Benefits**:
- Modular - Each concern in its own file (76% reduction in main build file)
- Reusable - Plugins can be shared across projects
- Maintainable - Easy to find and update configurations
- Type-safe - Full IDE support

### Project Configuration

| Item | Value |
|------|-------|
| Java Version | 25 |
| Kotlin Version | 2.2.20 (for buildSrc) |
| Gradle Version | 9.1.0 |
| Group ID | zm.hashcode |
| Artifact ID | open-payments-java |
| Version | 1.0-SNAPSHOT |

---

## Common Tasks

### Build Tasks

```bash
# Clean and build
./gradlew clean build

# Build without tests
./gradlew build -x test

# Build with verbose output
./gradlew build --info
```

### Test Tasks

```bash
# Run unit tests only
./gradlew test

# Run integration tests only
./gradlew integrationTest

# Run all tests (unit + integration)
./gradlew allTests

# Generate test report
./gradlew testReport

# Test with details (shows skipped tests)
./gradlew test --rerun-tasks
```

### Code Quality Tasks

```bash
# Format code automatically
./gradlew spotlessApply

# Check code formatting
./gradlew spotlessCheck

# Run Checkstyle
./gradlew checkstyleMain checkstyleTest

# Run PMD analysis
./gradlew pmdMain pmdTest

# View PMD reports
open build/reports/pmd/main.html
open build/reports/pmd/test.html

# Run all quality checks
./gradlew check
```

### Verification Tasks

```bash
# Check library availability
./gradlew checkLibraries

# Verify HTTP client implementations
./gradlew verifyHttpImplementations

# Run comprehensive health check
./gradlew healthCheck
```

### Publishing Tasks

```bash
# Publish to Maven Local
./gradlew publishToMavenLocal

# Publish to Maven Central (requires credentials)
./gradlew publish

# Full publishing workflow
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

---

## Code Quality Tools

### Tool Compatibility with Java 25

| Tool | Status | Version | Notes |
|------|--------|---------|-------|
| **Checkstyle** | ✅ Enabled | 11.1.0 | Fully supported |
| **Spotless** | ✅ Enabled | 8.0.0 | Fully supported |
| **PMD** | ✅ Enabled | 7.17.0 | Java 25 support since v7.17.0 |
| **JaCoCo** | ✅ Enabled | 0.8.13 | Fully supported |
| **SpotBugs** | ✅ Enabled | 4.9.6 | Java 25 compatible (warning mode) |
| **OWASP** | ✅ Enabled | 10.0.4 | Dependency scanning |
| **SonarQube** | ✅ Enabled | 5.1.0 | Code analysis platform |

### PMD Configuration

**Status**: Enforcing quality on main code (warning mode for tests)

**Current Violations**:
- Main code: 62 violations (under limit of 75)
- Test code: 217 violations (doesn't fail build)

**Key Exclusions**:
- `AvoidFieldNameMatchingMethodName` - False positive for Java records
- `AvoidDuplicateLiterals` - Common in tests
- `TooManyMethods` - Test classes can have many test methods

**Configuration**: `config/pmd/ruleset.xml`

**To view violations**:
```bash
./gradlew pmdMain pmdTest
open build/reports/pmd/main.html
```

### Checkstyle Configuration

**Rules**: Sun code conventions with modifications
**Max Warnings**: 20
**Configuration**: `config/checkstyle/checkstyle.xml`

**Key Rules**:
- Line length: 120 characters
- No star imports
- Braces required for all blocks
- Javadoc required for public APIs

### Spotless Configuration

**Formatter**: Eclipse JDT
**Configuration**: `config/spotless/eclipse-formatter.xml`

**Features**:
- Automatic import ordering
- Removes unused imports
- Consistent indentation (4 spaces)
- Trailing whitespace removal
- Newline at end of file

**Note**: Custom formatters disabled due to configuration cache serialization issues.

---

## Dependency Management

### Checking for Updates

**Use the wrapper script** (recommended):
```bash
./check-updates.sh
```

**Or manually** (requires special flags):
```bash
./gradlew dependencyUpdates --no-parallel --no-configuration-cache
```

**Why special flags?**
- The `gradle-versions-plugin` doesn't support configuration cache
- The plugin doesn't support parallel execution
- These are plugin limitations, not our configuration

### Updating Dependencies

**All versions are centralized** in:
```
buildSrc/src/main/kotlin/dependencies-convention.gradle.kts
```

**Example**:
```kotlin
val httpClient5Version = "5.5.1"
val jacksonVersion = "2.20.0"
val junitVersion = "6.0.0"

dependencies {
    implementation("org.apache.httpcomponents.client5:httpclient5:$httpClient5Version")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
}
```

**Update workflow**:
1. Edit versions in `dependencies-convention.gradle.kts`
2. Test changes: `./gradlew clean build test`
3. Review reports: `open build/reports/dependencyUpdates/report.html`

### Key Dependencies

| Category | Library | Version | Purpose |
|----------|---------|---------|---------|
| HTTP Client | Apache HttpClient 5 | 5.5.1 | Primary HTTP client |
| HTTP Client | OkHttp | 5.1.0 | Alternative HTTP client |
| JSON | Jackson | 2.20.0 | JSON serialization |
| Auth | HTTP Signatures | 1.8 | Request signing |
| Validation | Jakarta Validation | 3.1.1 | Bean validation |
| Testing | JUnit | 6.0.0 | Unit testing |
| Testing | Mockito | 5.20.0 | Mocking framework |

---

## Java 25 Support

### Current Status

**Fully supported** - All build tools work with Java 25

**Known Limitations**:
1. **Kotlin JVM Target Warning**:
   ```
   Kotlin does not yet support 25 JDK target, falling back to Kotlin JVM_24 JVM target
   ```
   - This is just a **warning** (not an error)
   - Kotlin 2.2.20 runs on Java 25 but compiles to JVM 24 target
   - Expected until Kotlin adds native Java 25 target support

2. **SpotBugs**:
   - Updated to version 4.9.6
   - Java 25 compatible - successfully analyzes Java 25 bytecode
   - Currently in warning mode (ignoreFailures = true)

### Toolchain Configuration

**Main Project**:
```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

**buildSrc** (for Kotlin):
```kotlin
kotlin {
    jvmToolchain(25)
}
```

---

## Troubleshooting

### Build Issues

#### "Kotlin does not yet support 25 JDK target"

**Status**: ⚠️ Warning only - builds still succeed

**Explanation**: Kotlin 2.2.20 runs on Java 25 but targets JVM 24

**Action**: No action needed

#### Configuration cache problems with dependencyUpdates

**Error**:
```
Configuration cache problems found
```

**Solution**: Use the wrapper script or add flags:
```bash
./check-updates.sh
# OR
./gradlew dependencyUpdates --no-parallel --no-configuration-cache
```

#### Parallel execution not supported

**Error**:
```
Parallel project execution is not supported
```

**Solution**: This is only for `dependencyUpdates`. Use `--no-parallel` flag or the wrapper script.

### Code Quality Issues

#### PMD violations in main code

**Current**: 62 violations (under limit of 75)

**To view**:
```bash
./gradlew pmdMain
open build/reports/pmd/main.html
```

**Most common violations**:
- `UseExplicitTypes` - Use explicit types instead of `var`
- `LiteralsFirstInComparisons` - Put literals on left side
- `MissingSerialVersionUID` - Add to exception classes

**To temporarily disable**:
```kotlin
// In static-analysis-convention.gradle.kts
pmd {
    isIgnoreFailures = true  // Don't fail build
}
```

#### Spotless formatting fails

**Solution**: Run format before building:
```bash
./gradlew spotlessApply
```

#### Checkstyle violations

**Solution**: Most violations auto-fixed by Spotless. Run:
```bash
./gradlew spotlessApply checkstyleMain
```

### buildSrc Changes Not Recognized

**Solution**: Clean buildSrc and rebuild:
```bash
./gradlew clean --stop
./gradlew build
```

---

## Performance Tips

### Build Performance

1. **Use Gradle Daemon** (enabled by default)
   - First build: slower
   - Subsequent builds: much faster

2. **Build Cache** (enabled in `gradle.properties`)
   ```properties
   org.gradle.caching=true
   ```

3. **Parallel Execution** (enabled by default)
   ```properties
   org.gradle.parallel=true
   ```

4. **Configuration Cache** (enabled by default)
   ```properties
   org.gradle.configuration-cache=true
   ```
   - Skip for `dependencyUpdates`: `--no-configuration-cache`

### Test Performance

```bash
# Run tests in parallel (default)
./gradlew test

# Increase test heap size (already set to 2g)
# See testing-convention.gradle.kts

# Skip tests during build
./gradlew build -x test
```

---

## Configuration Files

### Important Build Files

| File | Purpose |
|------|---------|
| `build.gradle.kts` | Main build configuration |
| `gradle.properties` | Gradle settings |
| `settings.gradle.kts` | Project settings |
| `buildSrc/` | Convention plugins |
| `config/checkstyle/checkstyle.xml` | Checkstyle rules |
| `config/pmd/ruleset.xml` | PMD rules |
| `config/spotless/eclipse-formatter.xml` | Code formatter |
| `.gitignore` | Git ignore patterns |

### Build Directories

| Directory | Purpose |
|-----------|---------|
| `build/` | Build outputs |
| `build/classes/` | Compiled classes |
| `build/libs/` | JAR files |
| `build/reports/` | Test & quality reports |
| `build/reports/tests/` | Test reports |
| `build/reports/pmd/` | PMD reports |
| `build/reports/checkstyle/` | Checkstyle reports |
| `.gradle/` | Gradle cache |

---

## Additional Resources

### Related Documentation

- [Development Setup Guide](SETUP.md) - Initial development environment setup
- [Code Quality Standards](CODE_QUALITY.md) - Detailed code quality rules
- [Contributing Guidelines](../CONTRIBUTING.md) - How to contribute
- [Architecture Guide](ARCHITECTURE.md) - System architecture

### External Resources

- [Gradle Documentation](https://docs.gradle.org/)
- [Gradle Convention Plugins](https://docs.gradle.org/current/userguide/custom_plugins.html#sec:convention_plugins)
- [PMD Rules](https://pmd.github.io/pmd/pmd_rules_java.html)
- [Checkstyle Checks](https://checkstyle.sourceforge.io/checks.html)

---

**Last Updated**: 2025-10-07
**Gradle Version**: 9.1.0
**Java Version**: 25
