import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask

/**
 * Convention plugin for static analysis tools.
 * Configures SpotBugs and PMD for code analysis.
 */

plugins {
    id("com.github.spotbugs")
    pmd
}

// SpotBugs Configuration - Static Analysis
// Updated to 4.9.6 - Java 25 support confirmed (class file major version 69)
// See: https://github.com/spotbugs/spotbugs/releases/tag/4.9.6
spotbugs {
    toolVersion = "4.9.6" // Latest version
    effort = Effort.MAX
    reportLevel = Confidence.LOW
    ignoreFailures = true // Warning mode - doesn't fail build
}

tasks.withType<SpotBugsTask>().configureEach {
    enabled = true // Java 25 compatible
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
// ENABLED: PMD 7.17.0+ supports Java 25 (class file major version 69)
// See: https://github.com/pmd/pmd/releases/tag/pmd_releases%2F7.17.0
pmd {
    toolVersion = "7.17.0" // Updated to support Java 25
    isConsoleOutput = true
    ruleSetFiles = files("${project.rootDir}/config/pmd/ruleset.xml")
    ruleSets = emptyList() // Use custom ruleset
    isIgnoreFailures = false // Now enforcing violations
    maxFailures = 75 // Allow up to 75 violations (62 main + buffer for refactoring)
}

tasks.withType<Pmd>().configureEach {
    enabled = true // Re-enabled for Java 25 support
    reports {
        html.required = true
        xml.required = true
    }
}

// Relax PMD for test code (less critical than production code)
tasks.named<Pmd>("pmdTest") {
    ignoreFailures = true // Don't fail build on test violations
}
