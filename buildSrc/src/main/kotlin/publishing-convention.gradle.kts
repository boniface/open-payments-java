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
