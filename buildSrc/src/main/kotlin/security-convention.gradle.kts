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
        directory = "${layout.buildDirectory.get().asFile}/dependency-check-data"
        // Use a connection string that handles locks better
        connectionString = "jdbc:h2:file:${layout.buildDirectory.get().asFile}/dependency-check-data/odc;AUTOCOMMIT=ON;MV_STORE=FALSE;FILE_LOCK=SERIALIZED"
    }

    // Retry configuration for transient failures
    nvd.maxRetryCount = 5
    nvd.delay = 5000

    // Timeout settings to prevent hanging
    nvd.validForHours = 24
}

// Add a task to clean the dependency-check database if corrupted
tasks.register("cleanDependencyCheckDb") {
    group = "verification"
    description = "Cleans the OWASP Dependency Check database to fix corruption issues"

    notCompatibleWithConfigurationCache("Task deletes files and doesn't benefit from caching")

    doLast {
        val dbDir = file("${layout.buildDirectory.get().asFile}/dependency-check-data")
        if (dbDir.exists()) {
            delete(dbDir)
            println("✅ Dependency Check database cleaned")
        } else {
            println("ℹ️  No database directory found to clean")
        }
    }
}

// Configure all dependency check tasks to be incompatible with configuration cache
tasks.withType<org.owasp.dependencycheck.gradle.tasks.AbstractAnalyze>().configureEach {
    notCompatibleWithConfigurationCache("OWASP Dependency Check plugin is not compatible with configuration cache")

    // Ensure database directory exists
    doFirst {
        val dbDir = file("${layout.buildDirectory.get().asFile}/dependency-check-data")
        if (!dbDir.exists()) {
            dbDir.mkdirs()
            println("📁 Created dependency-check database directory")
        }

        // Clean old database if it's too old or corrupted
        if (dbDir.exists()) {
            val lockFile = file("${dbDir}/odc.lock.db")
            if (lockFile.exists()) {
                println("⚠️  Found stale lock file, cleaning database...")
                delete(dbDir)
                dbDir.mkdirs()
            } else {
                val ageInDays = (System.currentTimeMillis() - dbDir.lastModified()) / (1000 * 60 * 60 * 24)
                if (ageInDays > 7) {
                    println("⚠️  Dependency Check database is ${ageInDays} days old, cleaning...")
                    delete(dbDir)
                    dbDir.mkdirs()
                }
            }
        }
    }

    // Clean up after execution
    doLast {
        println("✅ Dependency vulnerability check completed")
    }
}

// Make check task depend on dependency check analysis
tasks.named("check") {
    // Note: We don't make check depend on dependencyCheckAnalyze by default
    // as it's slow. Run it explicitly in CI or use a separate task.
}
