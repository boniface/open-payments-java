import com.diffplug.gradle.spotless.SpotlessExtension

/**
 * Convention plugin for code quality tools.
 * Configures Spotless (code formatting) and Checkstyle (style checking).
 */

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

        // Note: Indentation is handled by Eclipse formatter configuration
        // Custom formatting steps are commented out due to serialization issues with config cache
        // Fix tabs
        // replaceRegex("Fix tabs", "\t", "    ")

        // Fix star imports in test files
        // custom("fixStarImports") { contents ->
        //     contents
        //         .replace(
        //             "import org.junit.jupiter.api.Assertions.*;",
        //             "import org.junit.jupiter.api.Assertions.assertEquals;\n" +
        //                 "import org.junit.jupiter.api.Assertions.assertNotNull;\n" +
        //                 "import org.junit.jupiter.api.Assertions.assertThrows;\n" +
        //                 "import org.junit.jupiter.api.Assertions.assertTrue;\n" +
        //                 "import org.junit.jupiter.api.Assertions.assertFalse;\n" +
        //                 "import org.junit.jupiter.api.Assertions.fail;",
        //         )
        //         .replace(
        //             "import static org.junit.jupiter.api.Assertions.*;",
        //             "import static org.junit.jupiter.api.Assertions.assertEquals;\n" +
        //                 "import static org.junit.jupiter.api.Assertions.assertNotNull;\n" +
        //                 "import static org.junit.jupiter.api.Assertions.assertThrows;\n" +
        //                 "import static org.junit.jupiter.api.Assertions.assertTrue;\n" +
        //                 "import static org.junit.jupiter.api.Assertions.assertFalse;\n" +
        //                 "import static org.junit.jupiter.api.Assertions.fail;",
        //         )
        // }

        // Add braces to if statements for Checkstyle compliance
        // custom("addBracesToIf") { contents ->
        //     contents
        //         .replace(Regex("if \\(this == o\\) return true;"), "if (this == o) { return true; }")
        //         .replace(
        //             Regex("if \\(o == null \\|\\| getClass\\(\\) != o\\.getClass\\(\\)\\) return false;"),
        //             "if (o == null || getClass() != o.getClass()) { return false; }",
        //         )
        // }
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
