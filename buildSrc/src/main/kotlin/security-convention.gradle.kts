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

    // Use NVD API Key from environment variable if available
    nvd.apiKey = System.getenv("NVD_API_KEY") ?: ""
}
