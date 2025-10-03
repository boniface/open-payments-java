plugins {
    `java-library`
    `maven-publish`
    signing
    checkstyle
    id("com.diffplug.spotless")
    id("io.github.gradle-nexus.publish-plugin")
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

    // HTTP Client - Modern Java HTTP Client with virtual threads support
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4")

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
        }
    }

    // Integration tests - run separately with: ./gradlew integrationTest
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
    }

    // Run all tests (unit + integration) with: ./gradlew allTests
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

        // Import ordering
        importOrder("java", "javax", "jakarta", "org", "com", "")
        removeUnusedImports()

        // Basic formatting
        endWithNewline()
        trimTrailingWhitespace()

        // Fix tabs
        replaceRegex("Fix tabs", "\t", "    ")

        // Fix indentation
        indentWithSpaces(4)

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
    maxWarnings = 0
    sourceSets = listOf(project.sourceSets.main.get(), project.sourceSets.test.get())
}

tasks.withType<Checkstyle>().configureEach {
    // Don't depend on classes - Checkstyle only needs source files
    classpath = files()
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
