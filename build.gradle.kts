/**
 * Main build configuration for Open Payments Java SDK.
 * Uses convention plugins from buildSrc for modular configuration.
 */

plugins {
    // Convention plugins (defined in buildSrc)
    id("dependencies-convention")
    id("testing-convention")
    id("quality-convention")
    id("static-analysis-convention")
    id("coverage-convention")
    // id("security-convention") // Disabled: OWASP dependency-check incompatible with Gradle 9.1
    id("sonar-convention")
    id("publishing-convention")
    id("utilities-convention")
}

group = "zm.hashcode"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Custom Verification Tasks
// ═══════════════════════════════════════════════════════════════════════════════

tasks.register("checkLibraries") {
    group = "verification"
    description = "Checks that required HTTP client libraries are available"

    // Depend on classes to ensure dependencies are resolved
    dependsOn("classes")

    doLast {
        println("\n═══════════════════════════════════════════════════════════")
        println("  Library Availability Check")
        println("═══════════════════════════════════════════════════════════")

        val libraries =
            mapOf(
                "Apache HttpClient 5" to "org.apache.hc.client5.http.impl.async.HttpAsyncClients",
                "OkHttp" to "okhttp3.OkHttpClient",
                "Jackson Databind" to "com.fasterxml.jackson.databind.ObjectMapper",
                "HTTP Signatures" to "org.tomitribe.auth.signatures.Signer",
                "Jakarta Validation" to "jakarta.validation.Validator",
            )

        val results = mutableMapOf<String, Boolean>()

        libraries.forEach { (name, className) ->
            val available =
                try {
                    Class.forName(className)
                    true
                } catch (e: ClassNotFoundException) {
                    false
                }
            results[name] = available
            val status = if (available) "✓ Available" else "✗ Missing"
            println("  %-25s %s".format(name, status))
        }

        println("───────────────────────────────────────────────────────────")

        val allAvailable = results.values.all { it }
        if (allAvailable) {
            println("  Status: ✓ All required libraries are available")
        } else {
            val available = results.filterValues { it }.size
            val total = results.size
            println("  Status: $available/$total libraries available")
            println("  Note: Some libraries may not be loaded at build time")
            println("  Action: Run './gradlew build' to verify all dependencies")
        }
        println("═══════════════════════════════════════════════════════════\n")
    }
}

/**
 * Task to check for dependency updates and flag critical updates.
 *
 * IMPORTANT: This task requires special flags due to gradle-versions-plugin limitations:
 *   ./gradlew checkUpdates --no-parallel --no-configuration-cache
 *
 * OR use the convenient wrapper script:
 *   ./check-updates.sh
 */
tasks.register("checkUpdates") {
    group = "verification"
    description = "Checks for dependency updates (requires --no-parallel --no-configuration-cache)"
    dependsOn("dependencyUpdates")

    // Check if required flags are present
    doFirst {
        val hasParallel = gradle.startParameter.isParallelProjectExecutionEnabled
        val hasConfigCache = gradle.startParameter.isConfigurationCacheRequested

        if (hasParallel || hasConfigCache) {
            val missingFlags = mutableListOf<String>()
            if (hasParallel) missingFlags.add("--no-parallel")
            if (hasConfigCache) missingFlags.add("--no-configuration-cache")

            logger.error("\n╔═══════════════════════════════════════════════════════════╗")
            logger.error("║  ❌ ERROR: Missing Required Flags                         ║")
            logger.error("╚═══════════════════════════════════════════════════════════╝")
            logger.error("")
            logger.error("The dependencyUpdates task requires:")
            logger.error("  ${missingFlags.joinToString(" ")}")
            logger.error("")
            logger.error("Please run one of these commands instead:")
            logger.error("  1. ./gradlew checkUpdates --no-parallel --no-configuration-cache")
            logger.error("  2. ./check-updates.sh")
            logger.error("")
            throw GradleException("Missing required flags: ${missingFlags.joinToString(" ")}")
        }
    }

    doLast {
        println("\n═══════════════════════════════════════════════════════════")
        println("  Dependency Update Summary")
        println("═══════════════════════════════════════════════════════════")
        println("  Full report: build/reports/dependencyUpdates/report.html")
        println("  Run: open build/reports/dependencyUpdates/report.html")
        println("  ")
        println("  Quick commands:")
        println("  • Manual: ./gradlew dependencyUpdates --no-parallel --no-configuration-cache")
        println("  • Script: ./check-updates.sh")
        println("───────────────────────────────────────────────────────────")
        println("  To update: Edit versions in buildSrc/src/main/kotlin/dependencies-convention.gradle.kts")
        println("  Then run: ./gradlew clean build test")
        println("═══════════════════════════════════════════════════════════\n")
    }
}

/**
 * Task to verify HTTP client implementation selection.
 */
tasks.register("verifyHttpImplementations") {
    group = "verification"
    description = "Verifies HTTP client implementation availability and selection"

    doLast {
        println("\n═══════════════════════════════════════════════════════════")
        println("  HTTP Client Implementation Verification")
        println("═══════════════════════════════════════════════════════════")

        val implementations =
            mapOf(
                "APACHE" to "org.apache.hc.client5.http.impl.async.HttpAsyncClients",
                "OKHTTP" to "okhttp3.OkHttpClient",
            )

        implementations.forEach { (name, className) ->
            val available =
                try {
                    Class.forName(className)
                    true
                } catch (e: ClassNotFoundException) {
                    false
                }

            val status = if (available) "✓ Available" else "✗ Not Found"
            val recommendation =
                when {
                    name == "APACHE" && available -> "(Recommended for production)"
                    name == "OKHTTP" && available -> "(Lightweight alternative)"
                    else -> "(Add dependency to use)"
                }

            println("  %-10s %-15s %s".format(name, status, recommendation))
        }

        println("───────────────────────────────────────────────────────────")
        println("  See: HTTP_IMPLEMENTATION_SELECTION_GUIDE.md for details")
        println("═══════════════════════════════════════════════════════════\n")
    }
}

/**
 * Comprehensive health check task that runs all verification tasks.
 */
tasks.register("healthCheck") {
    group = "verification"
    description = "Runs comprehensive health check (libraries, updates, tests)"

    dependsOn("checkLibraries", "verifyHttpImplementations", "test")

    doLast {
        println("\n═══════════════════════════════════════════════════════════")
        println("  ✓ Health Check Complete")
        println("═══════════════════════════════════════════════════════════")
        println("  All systems operational")
        println("  Run './gradlew checkUpdates' to check for dependency updates")
        println("═══════════════════════════════════════════════════════════\n")
    }
}
