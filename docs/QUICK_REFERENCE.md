# Quick Reference - Code Quality & Formatting

[📚 Back to Documentation Index](INDEX.md) | [🏠 Back to README](../README.md)

---

## 🚀 One Command to Rule Them All

```bash
./gradlew build
```

This automatically:
1. Formats your code (Spotless)
2. Compiles with strict checks
3. Runs Checkstyle
4. Runs tests
5. Creates build artifacts

## 📝 Common Commands

```bash
# Format all code automatically
./gradlew spotlessApply

# Build from scratch
./gradlew clean build

# Run quality checks only
./gradlew check

# Check formatting without fixing
./gradlew spotlessCheck

# Run Checkstyle only
./gradlew checkstyleMain checkstyleTest
```

## 🔧 What Gets Formatted Automatically

- ✅ Import organization
- ✅ Unused import removal
- ✅ Indentation (4 spaces)
- ✅ Trailing whitespace
- ✅ Brace formatting
- ✅ File endings

## 📊 Quality Standards Enforced

- **Line length**: 120 characters max
- **Method length**: 150 lines max
- **Parameters**: 7 max per method
- **Imports**: No wildcards (*)
- **Naming**: Standard Java conventions
- **Structure**: No redundant modifiers, proper visibility

## 🔴 When Build Fails

### "Checkstyle rule violations found"
- Your code violates style rules
- Check the report: `build/reports/checkstyle/main.html`
- Fix manually or run: `./gradlew spotlessApply` and rebuild

### "Spotless check failed"
- Code is not formatted correctly
- Fix: `./gradlew spotlessApply`

### "Compilation failed"
- Java syntax errors
- Check compiler output for details

## 📁 Reports Location

After build, check these for detailed information:
- **Checkstyle**: `build/reports/checkstyle/`
- **PMD**: `build/reports/pmd/` (when enabled)
- **SpotBugs**: `build/reports/spotbugs/` (when enabled)

## 💡 Pro Tips

1. **Before committing**: Always run `./gradlew build`
2. **IDE Integration**: Configure your IDE to use the same Checkstyle rules
3. **Fast formatting**: `./gradlew spotlessApply` is faster than full build
4. **CI/CD**: The build fails on violations, perfect for pipelines

## ⚙️ Configuration Files

Want to customize rules? Edit these:
- `config/checkstyle/checkstyle.xml` - Style rules
- `config/pmd/ruleset.xml` - PMD rules
- `config/spotbugs/excludeFilter.xml` - SpotBugs exclusions
- `build.gradle.kts` - Spotless formatting rules

## 🆘 Troubleshooting

**Cache issues?**
```bash
rm -rf .gradle/configuration-cache
./gradlew clean build
```

**Daemon issues?**
```bash
./gradlew --stop
./gradlew clean build
```

**Still stuck?**
Check detailed docs in `CODE_QUALITY.md`
