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

    // Database configuration to fix H2 corruption issues
    data {
        directory = "${project.buildDir}/dependency-check-data"
        // Use a connection string that handles locks better
        connectionString = "jdbc:h2:file:${project.buildDir}/dependency-check-data/odc;AUTOCOMMIT=ON;MV_STORE=FALSE;FILE_LOCK=SERIALIZED"
    }

    // Retry configuration for transient failures
    nvd.maxRetryCount = 3
    nvd.delay = 3000
}

// Add a task to clean the dependency-check database if corrupted
tasks.register("cleanDependencyCheckDb") {
    group = "verification"
    description = "Cleans the OWASP Dependency Check database to fix corruption issues"
    doLast {
        delete("${project.buildDir}/dependency-check-data")
        println("✅ Dependency Check database cleaned")
    }
}

// Make dependencyCheckAnalyze depend on clean if the database exists and is old
tasks.named("dependencyCheckAnalyze") {
    doFirst {
        val dbDir = file("${project.buildDir}/dependency-check-data")
        if (dbDir.exists()) {
            val ageInDays = (System.currentTimeMillis() - dbDir.lastModified()) / (1000 * 60 * 60 * 24)
            if (ageInDays > 7) {
                println("⚠️  Dependency Check database is ${ageInDays} days old, cleaning...")
                delete(dbDir)
            }
        }
    }
}
