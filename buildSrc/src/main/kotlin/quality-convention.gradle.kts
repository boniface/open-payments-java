import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    checkstyle
    id("com.diffplug.spotless")
}

// Spotless - Automatic Code Formatting
configure<SpotlessExtension> {
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
}

tasks.withType<Checkstyle>().configureEach {
    // Don't depend on classes - Checkstyle only needs source files
    classpath = files()
}

// Task ordering configuration
tasks.withType<JavaCompile>().configureEach {
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
        release.set(25)
    }
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.named("checkstyleMain") {
    dependsOn("spotlessApply")
    mustRunAfter("spotlessApply")
}

tasks.named("checkstyleTest") {
    dependsOn("spotlessApply")
    mustRunAfter("spotlessApply")
}

tasks.named("check") {
    dependsOn("spotlessApply", "spotlessCheck", "checkstyleMain", "checkstyleTest")
    mustRunAfter("spotlessApply")
}
