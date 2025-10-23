/**
 * Convention plugin for security scanning.
 * Configures OWASP Dependency Check for vulnerability scanning.
 */

plugins {
    id("org.owasp.dependencycheck")
}

// OWASP Dependency Check - Security Vulnerabilities
dependencyCheck {
    autoUpdate = true
    format = "HTML"
    suppressionFile = "${project.rootDir}/config/dependency-check/suppressions.xml"
    failBuildOnCVSS = 7.0f
    analyzers.assemblyEnabled = false
}
