# Code Quality Tools

[📚 Back to Documentation Index](INDEX.md) | [🏠 Back to README](../README.md)

---

This project uses automated code quality tools to enforce best practices, style consistency, and identify potential bugs during the build process.

## Automatic Code Formatting

### Spotless (Active) 
- **Purpose**: Automatically formats code before compilation
- **Runs**: **Automatically during `build` and `compileJava`**
- **Features**:
  - Remove unused imports
  - Organize imports by package groups
  - Remove trailing whitespace
  - Ensure files end with newlines
  - Convert tabs to spaces (4-space indentation)
  - Fix brace formatting

**Manual Commands:**
```bash
# Automatically fix formatting issues
./gradlew spotlessApply

# Check formatting without fixing
./gradlew spotlessCheck
```

**Note**: Spotless runs automatically before every compilation, so you don't need to run it manually!

## Static Analysis Tools

### 1. Checkstyle (Active) 
- **Version**: 10.20.2
- **Purpose**: Enforces Java coding standards and style conventions
- **Runs**: Before compilation (on source files)
- **Configuration**: `config/checkstyle/checkstyle.xml`
- **Features**:
  - Naming conventions
  - Import management
  - Whitespace and formatting
  - Code structure best practices
  - Line length limit (120 characters)
  - Method length limit (150 lines)
  - Parameter count limit (7 parameters)

### 2. PMD
- **Version**: 7.17.0
- **Purpose**: Source code analyzer to find common programming flaws
- **Runs**: After compilation
- **Configuration**: `config/pmd/ruleset.xml`
- **Status**: ✅ Enabled - Java 25 support since v7.17.0
- **Enforcement**: Main code enforced (62 violations, limit 75), test code warnings only

### 3. SpotBugs
- **Version**: 4.9.6
- **Purpose**: Static analysis tool to find bugs in Java code
- **Runs**: After compilation
- **Configuration**: `config/spotbugs/excludeFilter.xml`
- **Status**: ✅ Enabled - Java 25 compatible, currently in warning mode

## Running Quality Checks

### Standard Build (Recommended)
This is all you need! Formatting and quality checks happen automatically:
```bash
./gradlew build
```

**What happens automatically:**
1. **Spotless** formats your code
2. Code is compiled
3. **Checkstyle** verifies code style
4. Tests run
5. Build artifacts are created

### Manual Commands

```bash
# Format code without building
./gradlew spotlessApply

# Check all quality rules
./gradlew check

# Run specific checks
./gradlew checkstyleMain checkstyleTest
./gradlew pmdMain pmdTest
./gradlew spotbugsMain spotbugsTest
```

### Execution Order
1. **spotlessApply** → Formats code automatically
2. **compileJava** → Compiles the formatted code
3. **checkstyleMain** → Verifies code style (after formatting)
4. **pmdMain** → Static analysis
5. **spotbugsMain** → Bug detection

## Reports

After running quality checks, reports are generated in:
- **Checkstyle**: `build/reports/checkstyle/`
- **PMD**: `build/reports/pmd/`
- **SpotBugs**: `build/reports/spotbugs/`

Open the HTML reports in your browser for detailed violation information.

## Customizing Rules

### Checkstyle
Edit `config/checkstyle/checkstyle.xml` to modify style rules.

### PMD
Edit `config/pmd/ruleset.xml` to enable/disable specific rule categories or individual rules.

### SpotBugs
Edit `config/spotbugs/excludeFilter.xml` to exclude specific bug patterns or files.

## Integration with CI/CD

These quality checks are configured to fail the build if violations are found, making them suitable for CI/CD pipelines:
- Checkstyle: `maxWarnings = 0`, `isIgnoreFailures = false`
- PMD: `isIgnoreFailures = false` (main code), `ignoreFailures = true` (test code)
- SpotBugs: `ignoreFailures = true` (warning mode)

## Java 25 Compatibility Note

Currently using Java 25 with full tool support:
- ✅ **Checkstyle**: Fully functional (source code analysis)
- ✅ **PMD**: Enabled (v7.17.0+ supports Java 25)
- ✅ **SpotBugs**: Enabled (v4.9.6 is Java 25 compatible)

All quality tools are now fully operational with Java 25.

## Best Practices

1. **Run checks locally** before committing code
2. **Fix violations immediately** rather than accumulating technical debt
3. **Review reports** to understand the reasoning behind violations
4. **Configure IDE** to use the same Checkstyle rules for real-time feedback
5. **Keep configurations updated** as the project evolves
