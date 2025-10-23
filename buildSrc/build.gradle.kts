plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

// Kotlin version for buildSrc - Kotlin 2.2.20 supports Java 25
kotlin {
    jvmToolchain(25)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.0.0")
    implementation("io.github.gradle-nexus:publish-plugin:2.0.0")
    implementation("org.owasp:dependency-check-gradle:10.0.4")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.2.5")
    implementation("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:5.1.0.4882")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.53.0")
    implementation("com.authlete:http-message-signatures:1.8")
}
