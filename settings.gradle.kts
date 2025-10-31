rootProject.name = "open-payments-java"

pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.20"
        id("com.diffplug.spotless") version "6.25.0"
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
