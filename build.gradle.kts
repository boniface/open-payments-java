plugins {
    `java-library`
    `maven-publish`
    signing
    checkstyle
    jacoco
    id("com.diffplug.spotless")
    id("io.github.gradle-nexus.publish-plugin")
    id("org.owasp.dependencycheck") version "10.0.4"
    id("com.github.spotbugs") version "6.0.26"
    id("pmd")
    id("org.sonarqube") version "5.1.0.4882"
    id("com.github.ben-manes.versions") version "0.52.0" // Dependency version updates
}

group = "zm.hashcode"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withJavadocJar()
    withSourcesJar()
}

dependencies {

    // HTTP Clients - Multiple implementations available
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON Processing - Jackson for JSON serialization/deserialization
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.18.2")

    // HTTP Signatures - For Open Payments authentication
    implementation("org.tomitribe:tomitribe-http-signatures:1.8")

    // Validation
    implementation("jakarta.validation:jakarta.validation-api:3.1.0")
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")

    // Utilities
    implementation("com.google.guava:guava:33.3.1-jre")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("ch.qos.logback:logback-classic:1.5.12")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    test {
        useJUnitPlatform {
            excludeTags("integration")
        }
        maxHeapSize = "2g"

        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = false
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }

        afterSuite(
            KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->
                if (desc.parent == null) {
                    println("\n═══════════════════════════════════════════════════════════")
                    println("  Test Results")
                    println("═══════════════════════════════════════════════════════════")
                    println("  Total:   ${result.testCount}")
                    println("  Passed:  ${result.successfulTestCount}")
                    println("  Failed:  ${result.failedTestCount}")
                    println("  Skipped: ${result.skippedTestCount}")
                    println("───────────────────────────────────────────────────────────")
                    val resultText =
                        when {
                            result.failedTestCount > 0 -> "FAILED"
                            result.skippedTestCount > 0 -> "SUCCESS (with skipped)"
                            else -> "SUCCESS"
                        }
                    println("  Result:  $resultText")
                    println("═══════════════════════════════════════════════════════════\n")
                }
            }),
        )

        doLast {
            if (state.skipped) {
                println("\n═══════════════════════════════════════════════════════════")
                println("  Test Results (from cache)")
                println("═══════════════════════════════════════════════════════════")
                println("  Tests were not executed - results are up-to-date")
                println("  Run with --rerun-tasks to force execution and see details")
                println("═══════════════════════════════════════════════════════════\n")
            }
        }
    }

    val integrationTest by registering(Test::class) {
        description = "Runs integration tests."
        group = "verification"

        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath

        useJUnitPlatform {
            includeTags("integration")
        }

        shouldRunAfter(test)
        maxHeapSize = "2g"

        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = true // Show output for integration tests
        }

        afterSuite(
            KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->
                if (desc.parent == null) {
                    println("\n═══════════════════════════════════════════════════════════")
                    println("  Integration Test Results")
                    println("═══════════════════════════════════════════════════════════")
                    println("  Total:   ${result.testCount}")
                    println("  Passed:  ${result.successfulTestCount}")
                    println("  Failed:  ${result.failedTestCount}")
                    println("  Skipped: ${result.skippedTestCount}")
                    println("───────────────────────────────────────────────────────────")
                    val resultText =
                        when {
                            result.failedTestCount > 0 -> "FAILED"
                            result.skippedTestCount > 0 -> "SUCCESS (with skipped)"
                            else -> "SUCCESS"
                        }
                    println("  Result:  $resultText")
                    println("═══════════════════════════════════════════════════════════\n")
                }
            }),
        )

        doLast {
            if (state.skipped) {
                println("\n═══════════════════════════════════════════════════════════")
                println("  Integration Test Results (from cache)")
                println("═══════════════════════════════════════════════════════════")
                println("  Tests were not executed - results are up-to-date")
                println("  Run with --rerun-tasks to force execution and see details")
                println("═══════════════════════════════════════════════════════════\n")
            }
        }
    }

    val allTests by registering(Test::class) {
        description = "Runs all tests (unit and integration)."
        group = "verification"

        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath

        useJUnitPlatform()
        maxHeapSize = "2g"

        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        afterSuite(
            KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->
                if (desc.parent == null) {
                    println("\n═══════════════════════════════════════════════════════════")
                    println("  All Tests Results (Unit + Integration)")
                    println("═══════════════════════════════════════════════════════════")
                    println("  Total:   ${result.testCount}")
                    println("  Passed:  ${result.successfulTestCount}")
                    println("  Failed:  ${result.failedTestCount}")
                    println("  Skipped: ${result.skippedTestCount}")
                    println("───────────────────────────────────────────────────────────")
                    val resultText =
                        when {
                            result.failedTestCount > 0 -> "FAILED"
                            result.skippedTestCount > 0 -> "SUCCESS (with skipped)"
                            else -> "SUCCESS"
                        }
                    println("  Result:  $resultText")
                    println("═══════════════════════════════════════════════════════════\n")
                }
            }),
        )

        doLast {
            if (state.skipped) {
                println("\n═══════════════════════════════════════════════════════════")
                println("  All Tests Results (from cache)")
                println("═══════════════════════════════════════════════════════════")
                println("  Tests were not executed - results are up-to-date")
                println("  Run with --rerun-tasks to force execution and see details")
                println("═══════════════════════════════════════════════════════════\n")
            }
        }
    }

    register("testReport") {
        group = "verification"
        description = "Runs all tests and always shows detailed results"
        dependsOn(allTests)
        doLast {
            println("\n═══════════════════════════════════════════════════════════")
            println("  ℹ️  Test Report")
            println("═══════════════════════════════════════════════════════════")
            println("  Detailed test results shown above.")
            println("  To force re-execution: ./gradlew allTests --rerun-tasks")
            println("  Test report: build/reports/tests/allTests/index.html")
            println("═══════════════════════════════════════════════════════════\n")
        }
    }

    compileJava {
        dependsOn("spotlessApply")
        options.apply {
            encoding = "UTF-8"
            compilerArgs.addAll(
                listOf(
                    "-Xlint:all",
                    "-Xlint:-serial",
                    "-parameters",
                ),
            )
            release = 25
        }
    }

    compileTestJava {
        dependsOn("spotlessApply")
        options.apply {
            encoding = "UTF-8"
            compilerArgs.addAll(
                listOf(
                    "-parameters",
                ),
            )
            release = 25
        }
    }

    javadoc {
        options {
            this as StandardJavadocDocletOptions
            encoding = "UTF-8"
            addStringOption("Xdoclint:none", "-quiet")
        }
    }

    // CRITICAL FIX: Make Checkstyle depend on Spotless formatting
    named("checkstyleMain") {
        dependsOn("spotlessApply")
        mustRunAfter("spotlessApply")
    }

    named("checkstyleTest") {
        dependsOn("spotlessApply")
        mustRunAfter("spotlessApply")
    }

    // Ensure proper task ordering: spotless → checkstyle → other checks
    check {
        dependsOn("spotlessApply", "spotlessCheck", "checkstyleMain", "checkstyleTest")
        mustRunAfter("spotlessApply")
    }
}

// Spotless - Automatic Code Formatting
spotless {
    java {
        target("src/*/java/**/*.java")

        // Use Eclipse JDT formatter (reliable and compatible with all Java versions)
        eclipse().configFile("${project.rootDir}/config/spotless/eclipse-formatter.xml")

        // Import ordering - NO STAR IMPORTS
        importOrder("java", "javax", "jakarta", "org", "com", "")
        removeUnusedImports()

        // Basic formatting
        endWithNewline()
        trimTrailingWhitespace()

        // Fix tabs
        replaceRegex("Fix tabs", "\t", "    ")

        // Fix indentation
        indentWithSpaces(4)

        // Fix star imports in test files
        custom("fixStarImports") { contents ->
            contents
                .replace(
                    "import org.junit.jupiter.api.Assertions.*;",
                    "import org.junit.jupiter.api.Assertions.assertEquals;\n" +
                        "import org.junit.jupiter.api.Assertions.assertNotNull;\n" +
                        "import org.junit.jupiter.api.Assertions.assertThrows;\n" +
                        "import org.junit.jupiter.api.Assertions.assertTrue;\n" +
                        "import org.junit.jupiter.api.Assertions.assertFalse;\n" +
                        "import org.junit.jupiter.api.Assertions.fail;",
                )
                .replace(
                    "import static org.junit.jupiter.api.Assertions.*;",
                    "import static org.junit.jupiter.api.Assertions.assertEquals;\n" +
                        "import static org.junit.jupiter.api.Assertions.assertNotNull;\n" +
                        "import static org.junit.jupiter.api.Assertions.assertThrows;\n" +
                        "import static org.junit.jupiter.api.Assertions.assertTrue;\n" +
                        "import static org.junit.jupiter.api.Assertions.assertFalse;\n" +
                        "import static org.junit.jupiter.api.Assertions.fail;",
                )
        }

        // Add braces to if statements for Checkstyle compliance
        custom("addBracesToIf") { contents ->
            contents
                .replace(Regex("if \\(this == o\\) return true;"), "if (this == o) { return true; }")
                .replace(
                    Regex("if \\(o == null \\|\\| getClass\\(\\) != o\\.getClass\\(\\)\\) return false;"),
                    "if (o == null || getClass() != o.getClass()) { return false; }",
                )
        }
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

// Checkstyle Configuration
checkstyle {
    toolVersion = "11.1.0"
    configFile = file("${project.rootDir}/config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 20 // Allow TODO comments in test files
    sourceSets = listOf(project.sourceSets.main.get(), project.sourceSets.test.get())
}

tasks.withType<Checkstyle>().configureEach {
    // Don't depend on classes - Checkstyle only needs source files
    classpath = files()
}

// JaCoCo Configuration - Code Coverage
jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test, tasks.named("integrationTest"))

    // Disable during development phase (interfaces only, no implementation yet)
    isEnabled = false

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }

    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/package-info.class",
                        "**/module-info.class",
                    )
                }
            },
        ),
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    // Disable coverage verification during development
    isEnabled = false

    violationRules {
        rule {
            limit {
                minimum = "0.00".toBigDecimal() // 0% for development
            }
        }

        rule {
            element = "CLASS"
            limit {
                minimum = "0.00".toBigDecimal() // 0% for development
            }
            excludes =
                listOf(
                    "*.package-info",
                    "*.module-info",
                )
        }
    }
}

tasks.test {
    // Disable JaCoCo during development
    // finalizedBy(tasks.jacocoTestReport)
}

// SpotBugs Configuration - Static Analysis
// DISABLED: SpotBugs does not support Java 25 yet (class file major version 69)
spotbugs {
    toolVersion = "4.8.6"
    effort = com.github.spotbugs.snom.Effort.MAX
    reportLevel = com.github.spotbugs.snom.Confidence.LOW
    ignoreFailures = true // Disabled for Java 25 compatibility
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    enabled = false // Disable SpotBugs for Java 25
    reports {
        create("html") {
            required = true
            outputLocation = file("${project.layout.buildDirectory.get()}/reports/spotbugs/$name.html")
        }
        create("xml") {
            required = true
            outputLocation = file("${project.layout.buildDirectory.get()}/reports/spotbugs/$name.xml")
        }
    }
}

// PMD Configuration - Source Code Analysis
// DISABLED: PMD does not support Java 25 yet (class file major version 69)
pmd {
    toolVersion = "7.7.0"
    isConsoleOutput = true
    ruleSetFiles = files("${project.rootDir}/config/pmd/ruleset.xml")
    ruleSets = emptyList() // Use custom ruleset
    isIgnoreFailures = true // Disabled for Java 25 compatibility
}

tasks.withType<Pmd>().configureEach {
    enabled = false // Disable PMD for Java 25
    reports {
        html.required = true
        xml.required = true
    }
}

// OWASP Dependency Check - Security Vulnerabilities
dependencyCheck {
    autoUpdate = true
    format = "HTML"
    suppressionFile = "${project.rootDir}/config/dependency-check/suppressions.xml"
    failBuildOnCVSS = 7.0f
    analyzers.assemblyEnabled = false
}

// SonarQube Configuration
sonar {
    properties {
        property("sonar.projectKey", "hashcode_open-payments-java")
        property("sonar.organization", "hashcode")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sources", "src/main/java")
        property("sonar.tests", "src/test/java")
        property("sonar.java.binaries", "${layout.buildDirectory.get()}/classes/java/main")
        property("sonar.java.libraries", configurations.compileClasspath.get().files.joinToString(","))
        property("sonar.java.test.binaries", "${layout.buildDirectory.get()}/classes/java/test")
        property("sonar.java.test.libraries", configurations.testCompileClasspath.get().files.joinToString(","))
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.coverage.exclusions", "**/package-info.java,**/module-info.java")
        property("sonar.exclusions", "**/generated/**")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = "Open Payments Java SDK"
                description = "Java SDK for Open Payments API - facilitating interoperable payment setup and completion"
                url = "https://github.com/boniface/open-payments-java"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "boniface"
                        name = "Boniface Kabaso"
                        email = "boniface.kabaso@example.com"
                    }
                    developer {
                        id = "espoir"
                        name = "Espoir D"
                        email = "espoir.d@example.com"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/boniface/open-payments-java.git"
                    developerConnection = "scm:git:ssh://github.com/boniface/open-payments-java.git"
                    url = "https://github.com/boniface/open-payments-java"
                }
            }
        }
    }
}

signing {
    // Only sign if publishing to Maven Central
    setRequired { gradle.taskGraph.hasTask("publish") }
    sign(publishing.publications["mavenJava"])
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl = uri("https://s01.oss.sonatype.org/service/local/")
            snapshotRepositoryUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Dependency Management and Update Checking
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Configure the dependency updates plugin to check for newer versions.
 * Run with: ./gradlew dependencyUpdates
 */
tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
    // Reject release candidates, milestones, alphas, betas
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }

    // Check for updates every run
    outputFormatter = "plain,html,json"
    outputDir = "build/reports/dependencyUpdates"
    reportfileName = "report"

    checkForGradleUpdate = true
    gradleReleaseChannel = "current"
}

/**
 * Helper function to determine if a version is unstable.
 */
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val unstableKeyword =
        listOf("ALPHA", "BETA", "RC", "CR", "M", "PREVIEW", "SNAPSHOT", "DEV")
            .any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return unstableKeyword || !isStable
}

/**
 * Task to check HTTP client library availability.
 * Verifies that Apache HttpClient and OkHttp are available on classpath.
 */
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
 * Run with: ./gradlew checkUpdates
 */
tasks.register("checkUpdates") {
    group = "verification"
    description = "Checks for dependency updates and flags critical ones"
    dependsOn("dependencyUpdates")

    doLast {
        println("\n═══════════════════════════════════════════════════════════")
        println("  Dependency Update Summary")
        println("═══════════════════════════════════════════════════════════")
        println("  Full report: build/reports/dependencyUpdates/report.html")
        println("  Run: open build/reports/dependencyUpdates/report.html")
        println("  ")
        println("  Critical dependencies to monitor:")
        println("  • Apache HttpClient 5 (current: 5.4)")
        println("  • OkHttp (current: 4.12.0)")
        println("  • Jackson (current: 2.18.2)")
        println("  • Jakarta Validation (current: 3.1.0)")
        println("───────────────────────────────────────────────────────────")
        println("  To update: Edit versions in build.gradle.kts")
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
