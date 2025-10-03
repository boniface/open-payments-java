rootProject.name = "open-payments-java"

pluginManagement {
    plugins {
        id("com.diffplug.spotless") version "6.25.0"
        id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
