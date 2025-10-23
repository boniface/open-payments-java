import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

/**
 * Convention plugin for utility tasks and dependency management.
 * Configures dependency update checking and custom verification tasks.
 */

plugins {
    id("com.github.ben-manes.versions")
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
 * Configure the dependency updates plugin to check for newer versions.
 * Run with: ./gradlew dependencyUpdates --no-parallel --no-configuration-cache
 *
 * Note: This task is not compatible with configuration cache and parallel execution.
 * Always run with --no-parallel and --no-configuration-cache flags.
 */
tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
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

    // Disable configuration cache for this task
    notCompatibleWithConfigurationCache("DependencyUpdatesTask accesses project at execution time")
}
