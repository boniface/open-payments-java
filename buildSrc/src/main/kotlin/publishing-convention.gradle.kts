import org.jreleaser.model.Active

plugins {
    java
    `maven-publish`
    id("org.jreleaser")
    id("io.github.sgtsilvio.gradle.metadata")
}

java {
    withSourcesJar()
    withJavadocJar()
}

metadata {
    readableName = "Open Payments Java SDK"
    description = "Java SDK for Open Payments API - facilitating interoperable payment setup and completion"
    license { apache2() }
    developers {
        register("boniface") {
            fullName.set("Boniface Kabaso")
            email.set("550236+boniface@users.noreply.github.com")
        }
        register("espoir") {
            fullName.set("Espoir Diteekemena")
            email.set("47171587+ESPOIR-DITE@users.noreply.github.com")
        }
    }
    github {
        org.set("hashcode-zm")
        repo.set("open-payments-java")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "zm.hashcode"
            artifactId = "open-payments-java"
            version = "0.1.0"

            from(components["java"])

            pom {
                name.set("Open Payments Java SDK")
                description.set("Java SDK for Open Payments API - facilitating interoperable payment setup and completion")
                url.set("https://github.com/hashcode-zm/open-payments-java")
                inceptionYear.set("2025")

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("boniface")
                        name.set("Boniface Kabaso")
                        email.set("550236+boniface@users.noreply.github.com")
                    }
                    developer {
                        id.set("espoir")
                        name.set("Espoir Diteekemena")
                        email.set("47171587+ESPOIR-DITE@users.noreply.github.com")
                    }
                }

                scm {
                    url.set("https://github.com/hashcode-zm/open-payments-java")
                    connection.set("scm:git:https://github.com/hashcode-zm/open-payments-java.git")
                    developerConnection.set("scm:git:ssh://git@github.com/hashcode-zm/open-payments-java.git")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    project {
        description.set("Java SDK for Open Payments API - facilitating interoperable payment setup and completion")
        authors.set(listOf("Boniface Kabaso", "Espoir Diteekemena"))
        license.set("Apache-2.0")
        links {
            homepage.set("https://github.com/hashcode-zm/open-payments-java")
        }
        inceptionYear.set("2025")
        copyright.set("2025 Boniface Kabaso")
    }

    release {
        github {
            repoOwner.set("hashcode-zm")
            name.set("open-payments-java")
            tagName.set("{{projectVersion}}")
            releaseName.set("Release {{projectVersion}}")
            overwrite.set(false)
            update {
                enabled.set(true)
            }
            changelog {
                enabled.set(true)
                formatted.set(Active.ALWAYS)
                preset.set("conventional-commits")
                contributors {
                    enabled.set(true)
                }
                append {
                    enabled.set(true)
                    target.set(file("CHANGELOG.md"))
                    content.set("""
                        ## [{{projectVersion}}] - {{releaseDate}}
                        {{changelogChanges}}
                        {{changelogContributors}}
                    """.trimIndent())
                }
            }
        }
    }

    signing {
        active = Active.ALWAYS
        armored = true
    }

    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active = Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())
                }
            }
        }
    }
}
