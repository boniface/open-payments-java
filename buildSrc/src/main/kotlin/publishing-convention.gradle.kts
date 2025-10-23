import java.time.Duration

plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
}

java {
    withJavadocJar()
    withSourcesJar()
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
                        email = "550236+boniface@users.noreply.github.com"
                    }
                    developer {
                        id = "espoir"
                        name = "Espoir Diteekemena"
                        email = "47171587+ESPOIR-DITE@users.noreply.github.com"
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
            // Maven Central Portal
            nexusUrl = uri("https://central.sonatype.com")

            // Use Central Portal token authentication
            // Set via environment variables or ~/.gradle/gradle.properties:
            // centralPortalUsername=<your-token-username>
            // centralPortalPassword=<your-token-password>
            username = project.findProperty("centralPortalUsername") as String?
                ?: System.getenv("CENTRAL_PORTAL_USERNAME")
            password = project.findProperty("centralPortalPassword") as String?
                ?: System.getenv("CENTRAL_PORTAL_PASSWORD")
        }
    }

    // Timeout configuration for large uploads
    connectTimeout = Duration.ofMinutes(3)
    clientTimeout = Duration.ofMinutes(3)
}
